package com.lifeshop.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String userAvatar;
    private String realName;
    private Integer gender;
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;
    private String email;
    private String address;
}
