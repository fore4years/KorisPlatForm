package com.generator.rental.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdmin("admin_01", "password123", "13800009991", "System Admin 1");
        createAdmin("admin_02", "password123", "13800009992", "System Admin 2");
    }

    private void createAdmin(String username, String password, String phone, String name) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count == 0) {
            User admin = new User();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setPhone(phone);
            admin.setRole(User.Role.ADMIN);
            admin.setName(name);
            admin.setStatus(User.Status.ACTIVE);
            admin.setUserId("koris" + java.util.UUID.randomUUID().toString());
            userMapper.insert(admin);
            System.out.println("Created admin user: " + username);
        } else {
            System.out.println("Admin user already exists: " + username);
        }
    }
}
