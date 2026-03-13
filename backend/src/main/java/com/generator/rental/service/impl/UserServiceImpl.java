package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.dto.LoginRequest;
import com.generator.rental.dto.MerchantApplicationRequest;
import com.generator.rental.dto.MerchantApplicationResponse;
import com.generator.rental.dto.RegisterRequest;
import com.generator.rental.dto.UserResponse;
import com.generator.rental.dto.UserUpdateRequest;
import com.generator.rental.entity.MerchantApplication;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.MerchantApplicationMapper;
import com.generator.rental.mapper.UserMapper;
import com.generator.rental.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private MerchantApplicationMapper merchantApplicationMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String FORGOT_PASSWORD_PREFIX = "forgot_password:";
    private static final String AUTH_TOKEN_PREFIX = "auth:token:";

    @Override
    public UserResponse register(RegisterRequest request) {
        if (count(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        if (count(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())) > 0) {
            throw new RuntimeException("手机号已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Generate userid: koris + uuid
        user.setUserId("koris" + UUID.randomUUID().toString());

        save(user);

        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        response.setUserid(user.getUserId());
        return response;
    }

    @Override
    public UserResponse login(LoginRequest request) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // Generate Token
        String token = UUID.randomUUID().toString();
        long ttl = (request.getRememberMe() != null && request.getRememberMe()) ? 7 * 24 * 60 * 60 : 24 * 60 * 60; // 7 days or 1 day

        // Store in Redis
        redisTemplate.opsForValue().set(AUTH_TOKEN_PREFIX + token, user.getId().toString(), ttl, TimeUnit.SECONDS);

        UserResponse response = convertToResponse(user);
        response.setToken(token);
        return response;
    }

    @Override
    public UserResponse getProfile(String userId) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToResponse(user);
    }

    @Override
    public UserResponse updateProfile(String userId, UserUpdateRequest request) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (request.getName() != null) user.setName(request.getName());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getCompanyName() != null) user.setCompanyName(request.getCompanyName());
        if (request.getContactPerson() != null) user.setContactPerson(request.getContactPerson());
        if (request.getBusinessScope() != null) user.setBusinessScope(request.getBusinessScope());
        if (request.getDeliveryRange() != null) user.setDeliveryRange(request.getDeliveryRange());

        // Handle sensitive fields
        if (request.getIdentityCard() != null) user.setIdentityCard(request.getIdentityCard());
        if (request.getBusinessLicense() != null) user.setBusinessLicense(request.getBusinessLicense());

        updateById(user);
        return convertToResponse(user);
    }

    @Override
    public MerchantApplicationResponse submitMerchantApplication(String userId, MerchantApplicationRequest request) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否有未处理的申请
        Long count = merchantApplicationMapper.selectCount(new LambdaQueryWrapper<MerchantApplication>()
                .eq(MerchantApplication::getUserId, user.getId())
                .eq(MerchantApplication::getStatus, MerchantApplication.Status.PENDING));

        if (count > 0) {
            throw new RuntimeException("您已有一个申请正在审核中");
        }

        MerchantApplication app = new MerchantApplication();
        BeanUtils.copyProperties(request, app);
        app.setUserId(user.getId());
        app.setStatus(MerchantApplication.Status.PENDING);

        merchantApplicationMapper.insert(app);

        // 填写用户信息
        app.setUser(user);
        return convertToAppResponse(app);
    }

    @Override
    public MerchantApplicationResponse getMyMerchantApplication(String userId) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) return null;

        MerchantApplication app = merchantApplicationMapper.selectOne(new LambdaQueryWrapper<MerchantApplication>()
                .eq(MerchantApplication::getUserId, user.getId())
                .orderByDesc(MerchantApplication::getCreateTime)
                .last("LIMIT 1"));

        if (app != null) {
            app.setUser(user);
            return convertToAppResponse(app);
        }
        return null;
    }

    @Override
    public void sendForgotPasswordCode(String phone) {
        if (count(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) == 0) {
            throw new RuntimeException("手机号未注册");
        }

        // Check frequency
        String key = FORGOT_PASSWORD_PREFIX + phone;
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (expire != null && expire > 240) {
            throw new RuntimeException("请稍后再试");
        }

        // Generate Code
        String code = String.format("%06d", new Random().nextInt(999999));

        // Save to Redis
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        // Mock SMS Send
        System.out.println(">>> Sending SMS to " + phone + ": Your verification code is " + code);
    }

    @Override
    public void resetPassword(String phone, String code, String newPassword) {
        String key = FORGOT_PASSWORD_PREFIX + phone;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        if (!savedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }

        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);

        // Delete code after usage
        redisTemplate.delete(key);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        response.setUserid(user.getUserId());

        // Mask sensitive info
        if (response.getIdentityCard() != null && response.getIdentityCard().length() > 10) {
            String id = response.getIdentityCard();
            response.setIdentityCard(id.substring(0, 3) + "***********" + id.substring(id.length() - 4));
        }
        if (response.getBusinessLicense() != null && response.getBusinessLicense().length() > 8) {
            String license = response.getBusinessLicense();
            response.setBusinessLicense(license.substring(0, 2) + "****" + license.substring(license.length() - 4));
        }

        return response;
    }

    private MerchantApplicationResponse convertToAppResponse(MerchantApplication app) {
        MerchantApplicationResponse response = new MerchantApplicationResponse();
        BeanUtils.copyProperties(app, response);
        if (app.getUser() != null) {
            response.setUserId(app.getUser().getUserId());
            response.setUsername(app.getUser().getUsername());
        }
        response.setCreatedAt(app.getCreateTime());
        return response;
    }
}
