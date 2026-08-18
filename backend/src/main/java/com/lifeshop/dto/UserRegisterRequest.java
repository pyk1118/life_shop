package com.lifeshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 4, max = 50, message = "用户账号长度应为4-50")
    private String userAccount;

    @NotBlank(message = "登录密码不能为空")
    @Size(min = 6, max = 20, message = "登录密码长度应为6-20")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;
}
