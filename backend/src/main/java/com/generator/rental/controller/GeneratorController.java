package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.dto.GeneratorDTO;
import com.generator.rental.dto.GeneratorDetailDTO;
import com.generator.rental.dto.GeneratorSearchRequest;
import com.generator.rental.entity.Generator;
import com.generator.rental.service.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generators")
public class GeneratorController {

    @Autowired
    private GeneratorService generatorService;

    /**
     * 搜索发电机
     *
     * @param request 搜索请求参数
     * @return 发电机列表
     */
    @GetMapping("/search")
    public Result<List<GeneratorDTO>> search(@ModelAttribute GeneratorSearchRequest request) {
        return Result.success(generatorService.searchGenerators(request));
    }

    /**
     * 获取发电机详情
     *
     * @param id 发电机ID
     * @return 发电机详情 DTO
     */
    @GetMapping("/{id}")
    public Result<GeneratorDetailDTO> getDetail(@PathVariable Long id) {
        return Result.success(generatorService.getGeneratorDetail(id));
    }

    /**
     * 获取商户的发电机列表
     *
     * @param merchantUserId 商户用户ID
     * @return 发电机列表
     */
    @GetMapping("/merchant/{merchantUserId}")
    public Result<List<GeneratorDTO>> getMyGenerators(@PathVariable String merchantUserId) {
        return Result.success(generatorService.getMyGenerators(merchantUserId));
    }

    /**
     * 发布发电机
     *
     * @param generator 发电机实体
     * @param merchantUserId 商户用户ID
     * @return 空响应
     */
    @PostMapping
    public Result<Void> createGenerator(@RequestBody Generator generator, @RequestHeader("X-User-ID") String merchantUserId) {
        generatorService.createGenerator(generator, merchantUserId);
        return Result.success();
    }

    /**
     * 更新发电机信息
     *
     * @param id 发电机ID
     * @param generator 发电机实体
     * @return 空响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateGenerator(@PathVariable Long id, @RequestBody Generator generator) {
        generatorService.updateGenerator(id, generator);
        return Result.success();
    }

    /**
     * 删除发电机
     *
     * @param id 发电机ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGenerator(@PathVariable Long id) {
        generatorService.deleteGenerator(id);
        return Result.success();
    }
}
