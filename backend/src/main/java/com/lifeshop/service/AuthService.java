package com.lifeshop.service;

import com.lifeshop.dto.LoginRequest;
import com.lifeshop.dto.PasswordChangeRequest;
import com.lifeshop.dto.UserRegisterRequest;
import com.lifeshop.vo.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void registerUser(UserRegisterRequest request);

    void changeUserPassword(Long userId, PasswordChangeRequest request);

    void changeMerchantPassword(Long merchantId, PasswordChangeRequest request);

    void changeAdminPassword(Long adminId, PasswordChangeRequest request);
}
