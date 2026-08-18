package com.lifeshop.security;

import com.lifeshop.common.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUser {
    private Long id;
    private String account;
    private RoleType role;
}
