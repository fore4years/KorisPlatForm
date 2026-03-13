package com.generator.rental.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Starting Data Migration Check...");
        
        List<User> users = userMapper.selectList(null);
        boolean updated = false;
        
        for (User user : users) {
            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                user.setUserId("koris" + UUID.randomUUID().toString());
                userMapper.updateById(user);
                System.out.println("Generated userId for user: " + user.getUsername() + " -> " + user.getUserId());
                updated = true;
            }
        }
        
        if (updated) {
            System.out.println(">>> Data Migration Completed: User IDs generated.");
        } else {
            System.out.println(">>> No migration needed.");
        }
    }
}
