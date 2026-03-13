package com.generator.rental.dto;

import com.generator.rental.entity.User;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String phone;
    private User.Role role;
    private String name;
    private String identityCard;
    private String businessLicense;
}
