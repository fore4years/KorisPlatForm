package com.generator.rental.dto;

import com.generator.rental.entity.User;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String userid; // New global user ID
    private String username;
    private String phone;
    private User.Role role;
    private String name;
    private User.Status status;
    private String avatar;
    private String address;
    private String companyName;
    private String contactPerson;
    private String businessScope;
    private String deliveryRange;
    private String identityCard;
    private String businessLicense;
    private String token;
}
