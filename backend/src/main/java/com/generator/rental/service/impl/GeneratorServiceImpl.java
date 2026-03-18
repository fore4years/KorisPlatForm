package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.dto.GeneratorDTO;
import com.generator.rental.dto.GeneratorDetailDTO;
import com.generator.rental.dto.GeneratorSearchRequest;
import com.generator.rental.entity.Generator;
import com.generator.rental.entity.Review;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.GeneratorMapper;
import com.generator.rental.mapper.ReviewMapper;
import com.generator.rental.mapper.UserMapper;
import com.generator.rental.service.GeneratorService;
import com.generator.rental.utils.RedisUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeneratorServiceImpl extends ServiceImpl<GeneratorMapper, Generator> implements GeneratorService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public List<GeneratorDTO> searchGenerators(GeneratorSearchRequest request) {
        LambdaQueryWrapper<Generator> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getPower())) {
            queryWrapper.eq(Generator::getPower, request.getPower());
        }
        if (request.getMinRent() != null) {
            queryWrapper.ge(Generator::getDailyRent, request.getMinRent());
        }
        if (request.getMaxRent() != null) {
            queryWrapper.le(Generator::getDailyRent, request.getMaxRent());
        }
        if (StringUtils.hasText(request.getLocation())) {
            queryWrapper.like(Generator::getAddress, request.getLocation());
        }
        
        // Only show approved and available stock
        queryWrapper.eq(Generator::getAuditStatus, Generator.AuditStatus.APPROVED);
        queryWrapper.eq(Generator::getStockStatus, Generator.StockStatus.AVAILABLE);

        // Sort
        if ("price_asc".equals(request.getSortType())) {
            queryWrapper.orderByAsc(Generator::getDailyRent);
        } else if ("price_desc".equals(request.getSortType())) {
            queryWrapper.orderByDesc(Generator::getDailyRent);
        }

        List<Generator> generators = list(queryWrapper);
        
        // Post-processing for Distance and Rating sorting
        List<GeneratorDTO> dtos = generators.stream().map(g -> {
            GeneratorDTO dto = new GeneratorDTO();
            BeanUtils.copyProperties(g, dto);
            
            // Calculate distance if user coords provided
            if (request.getUserLatitude() != null && request.getUserLongitude() != null 
                    && g.getLatitude() != null && g.getLongitude() != null) {
                dto.setDistance(calculateDistance(
                        request.getUserLatitude().doubleValue(), request.getUserLongitude().doubleValue(),
                        g.getLatitude().doubleValue(), g.getLongitude().doubleValue()));
            }
            
            // Fetch Avg Rating
            // Note: In MP, we can't easily do AVG query in wrapper without XML or custom select.
            // For now, we fetch all reviews and calc avg, or add a custom method in mapper.
            // Let's use a simple fetch for now as review count is likely small.
            Double avgRating = getAverageRating(g.getId());
            dto.setAverageRating(avgRating != null ? avgRating : 0.0);
            
            return dto;
        }).collect(Collectors.toList());

        if ("distance".equals(request.getSortType())) {
            dtos.sort(Comparator.comparing(GeneratorDTO::getDistance, Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("rating".equals(request.getSortType())) {
            dtos.sort(Comparator.comparing(GeneratorDTO::getAverageRating, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        return dtos;
    }

    @Override
    public GeneratorDetailDTO getGeneratorDetail(Long id) {
        String cacheKey = "generator:detail:" + id;
        
        Object cachedObj = redisUtils.get(cacheKey);
        if (cachedObj != null) {
            return (GeneratorDetailDTO) cachedObj;
        }

        Generator generator = getById(id);
        if (generator == null) {
            throw new RuntimeException("Generator not found");
        }

        // Fetch Merchant
        User merchant = userMapper.selectById(generator.getMerchantId());
        generator.setMerchant(merchant);

        GeneratorDetailDTO dto = new GeneratorDetailDTO();
        BeanUtils.copyProperties(generator, dto);

        // Merchant Info
        if (merchant != null) {
            GeneratorDetailDTO.MerchantInfo merchantInfo = new GeneratorDetailDTO.MerchantInfo();
            merchantInfo.setId(merchant.getId());
            merchantInfo.setName(merchant.getName());
            merchantInfo.setPhone(merchant.getPhone());
            dto.setMerchant(merchantInfo);
        }

        // Reviews
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>().eq(Review::getGeneratorId, id));
        List<GeneratorDetailDTO.ReviewDTO> reviewDTOs = reviews.stream().map(r -> {
            GeneratorDetailDTO.ReviewDTO rDto = new GeneratorDetailDTO.ReviewDTO();
            rDto.setRating(r.getRating());
            rDto.setComment(r.getComment());
            
            // Fetch Tenant Name
            User tenant = userMapper.selectById(r.getTenantId());
            rDto.setUsername(tenant != null ? tenant.getUsername() : "Unknown");
            rDto.setCreatedAt(r.getCreateTime() != null ? r.getCreateTime().toString() : null);
            return rDto;
        }).collect(Collectors.toList());
        dto.setReviews(reviewDTOs);
        
        Double avgRating = getAverageRating(id);
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);

        redisUtils.setWithRandomExpire(cacheKey, dto, 1800);

        return dto;
    }
    
    @Override
    public List<GeneratorDTO> getMyGenerators(String merchantUserId) {
        User merchant = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, merchantUserId));
        if (merchant == null) throw new RuntimeException("Merchant not found");

        List<Generator> generators = list(new LambdaQueryWrapper<Generator>().eq(Generator::getMerchantId, merchant.getId()));
        return generators.stream().map(g -> {
            GeneratorDTO dto = new GeneratorDTO();
            BeanUtils.copyProperties(g, dto);
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void createGenerator(Generator generator, String merchantUserId) {
        User merchant = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, merchantUserId));
        if (merchant == null) throw new RuntimeException("Merchant not found");
        
        generator.setMerchantId(merchant.getId());
        if (generator.getStockStatus() == null) {
            generator.setStockStatus(Generator.StockStatus.AVAILABLE);
        }
        save(generator);
    }

    @Override
    public void updateGenerator(Long id, Generator newGen) {
        Generator existing = getById(id);
        if (existing == null) throw new RuntimeException("Generator not found");
        
        BeanUtils.copyProperties(newGen, existing, "id", "merchantId", "createTime", "updateTime", "deleted");
        updateById(existing);
        
        redisUtils.del("generator:detail:" + id);
    }

    @Override
    public void deleteGenerator(Long id) {
        removeById(id);
        redisUtils.del("generator:detail:" + id);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Double getAverageRating(Long generatorId) {
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>().eq(Review::getGeneratorId, generatorId));
        if (reviews.isEmpty()) return 0.0;
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }
}
