package com.generator.rental.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.generator.rental.entity.Generator;
import com.generator.rental.entity.Order;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.GeneratorMapper;
import com.generator.rental.mapper.OrderMapper;
import com.generator.rental.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataFixRunner implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GeneratorMapper generatorMapper;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Starting Data Fix ===");

        // 0. Fix Passwords (Plain Text -> BCrypt)
        List<User> allUsers = userMapper.selectList(null);
        for (User user : allUsers) {
            boolean changed = false;
            
            // Fix: Generate userId if missing
            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                user.setUserId("koris" + java.util.UUID.randomUUID().toString());
                changed = true;
                System.out.println("Fixed: Generated userId for user " + user.getUsername());
            }

            String currentPassword = user.getPassword();
            // Check if password is NOT BCrypt encoded
            if (currentPassword != null && !currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$") && !currentPassword.startsWith("$2y$")) {
                String encodedPassword = passwordEncoder.encode(currentPassword);
                user.setPassword(encodedPassword);
                changed = true;
                System.out.println("Fixed: Encoded password for user " + user.getUsername());
            }
            
            if (changed) {
                userMapper.updateById(user);
            }
        }

        // 1. Fix merchant_bj role
        User merchant = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "merchant_bj"));
        if (merchant != null) {
            if (merchant.getRole() != User.Role.MERCHANT) {
                merchant.setRole(User.Role.MERCHANT);
                userMapper.updateById(merchant);
                System.out.println("Fixed: merchant_bj role updated to MERCHANT");
            }

            // 2. Assign all orders to merchant_bj (for testing purpose)
            List<Order> allOrders = orderMapper.selectList(null);
            for (Order order : allOrders) {
                if (order.getMerchantId() == null || !order.getMerchantId().equals(merchant.getId())) {
                    order.setMerchantId(merchant.getId());
                    orderMapper.updateById(order);
                    System.out.println("Fixed: Order " + order.getId() + " assigned to merchant_bj");
                }
            }

            // 3. Assign all generators to merchant_bj (for testing purpose)
            List<Generator> allGenerators = generatorMapper.selectList(null);
            for (Generator gen : allGenerators) {
                boolean genChanged = false;
                if (gen.getMerchantId() == null || !gen.getMerchantId().equals(merchant.getId())) {
                    gen.setMerchantId(merchant.getId());
                    genChanged = true;
                    System.out.println("Fixed: Generator " + gen.getId() + " assigned to merchant_bj");
                }
                // Fix: Reset invalid audit status
                if (gen.getAuditStatus() == null || !java.util.Arrays.asList(Generator.AuditStatus.values()).contains(gen.getAuditStatus())) {
                    gen.setAuditStatus(Generator.AuditStatus.PENDING);
                    genChanged = true;
                    System.out.println("Fixed: Generator " + gen.getId() + " audit status reset to PENDING");
                }
                if (genChanged) {
                    generatorMapper.updateById(gen);
                }
            }

        } else {
            System.out.println("Warning: merchant_bj user not found!");
        }

        // 4. Fix tenant_01 role and assign orders to it (for testing purpose)
        User tenant = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "tenant_01"));
        if (tenant != null) {
            if (tenant.getRole() != User.Role.TENANT) {
                tenant.setRole(User.Role.TENANT);
                userMapper.updateById(tenant);
                System.out.println("Fixed: tenant_01 role updated to TENANT");
            }
            
            // Assign all orders to tenant_01
            List<Order> allOrders = orderMapper.selectList(null);
            for (Order order : allOrders) {
                if (order.getTenantId() == null || !order.getTenantId().equals(tenant.getId())) {
                    order.setTenantId(tenant.getId());
                    orderMapper.updateById(order);
                    System.out.println("Fixed: Order " + order.getId() + " assigned to tenant_01");
                }
            }
        }

        System.out.println("=== Data Fix Completed ===");
    }
}
