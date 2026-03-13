package com.generator.rental.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String phone;
    private String code;
    private String newPassword;
}
