package com.lifeshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeshop.common.enums.RoleType;
import com.lifeshop.common.exception.BusinessException;
import com.lifeshop.common.result.ResultCode;
import com.lifeshop.dto.LoginRequest;
import com.lifeshop.dto.PasswordChangeRequest;
import com.lifeshop.dto.UserRegisterRequest;
import com.lifeshop.entity.BizMerchant;
import com.lifeshop.entity.BizUser;
import com.lifeshop.entity.SysAdmin;
import com.lifeshop.mapper.BizMerchantMapper;
import com.lifeshop.mapper.BizUserMapper;
import com.lifeshop.mapper.SysAdminMapper;
import com.lifeshop.security.JwtTokenManager;
import com.lifeshop.service.AuthService;
import com.lifeshop.vo.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final BizUserMapper bizUserMapper;
    private final BizMerchantMapper bizMerchantMapper;
    private final SysAdminMapper sysAdminMapper;
    private final JwtTokenManager jwtTokenManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        RoleType roleType = parseRole(request.getRole());
        return switch (roleType) {
            case USER -> loginUser(request);
            case MERCHANT -> loginMerchant(request);
            case ADMIN -> loginAdmin(request);
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(UserRegisterRequest request) {
        if (!request.getUserPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次输入密码不一致");
        }
        BizUser existUser = bizUserMapper.selectOne(new LambdaQueryWrapper<BizUser>()
                .eq(BizUser::getUserAccount, request.getUserAccount()));
        if (existUser != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户账号已存在");
        }
        BizUser user = new BizUser();
        user.setUserAccount(request.getUserAccount());
        user.setUserPassword(request.getUserPassword());
        user.setPhone(request.getPhone());
        user.setRealName("");
        user.setEmail("");
        user.setAddress("");
        user.setIsMerchant(0);
        user.setStatus(1);
        bizUserMapper.insert(user);
    }

    @Override
    public void changeUserPassword(Long userId, PasswordChangeRequest request) {
        BizUser user = bizUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        updatePassword(user.getUserPassword(), request, encoded -> {
            user.setUserPassword(encoded);
            bizUserMapper.updateById(user);
        });
    }

    @Override
    public void changeMerchantPassword(Long merchantId, PasswordChangeRequest request) {
        BizMerchant merchant = bizMerchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商家不存在");
        }
        updatePassword(merchant.getMerchantPassword(), request, encoded -> {
            merchant.setMerchantPassword(encoded);
            bizMerchantMapper.updateById(merchant);
        });
    }

    @Override
    public void changeAdminPassword(Long adminId, PasswordChangeRequest request) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开发者账号不存在");
        }
        updatePassword(admin.getAdminPassword(), request, encoded -> {
            admin.setAdminPassword(encoded);
            sysAdminMapper.updateById(admin);
        });
    }

    private LoginResponse loginUser(LoginRequest request) {
        BizUser user = bizUserMapper.selectOne(new LambdaQueryWrapper<BizUser>()
                .eq(BizUser::getUserAccount, request.getAccount()));
        if (user == null || user.getStatus() != 1 || !request.getPassword().equals(user.getUserPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户账号或密码错误");
        }
        String token = jwtTokenManager.createToken(Map.of(
                "id", user.getUserId(),
                "account", user.getUserAccount(),
                "role", RoleType.USER.name()));
        return LoginResponse.builder()
                .token(token)
                .role(RoleType.USER.name())
                .id(user.getUserId())
                .account(user.getUserAccount())
                .avatar(user.getUserAvatar())
                .build();
    }

    private LoginResponse loginMerchant(LoginRequest request) {
        BizMerchant merchant = bizMerchantMapper.selectOne(new LambdaQueryWrapper<BizMerchant>()
                .eq(BizMerchant::getMerchantAccount, request.getAccount()));
        if (merchant == null || merchant.getMerchantStatus() != 1
                || !request.getPassword().equals(merchant.getMerchantPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商家账号或密码错误");
        }
        if (merchant.getAuditStatus() == null || merchant.getAuditStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "入驻申请尚未审核通过，无法登录");
        }
        String token = jwtTokenManager.createToken(Map.of(
                "id", merchant.getMerchantId(),
                "account", merchant.getMerchantAccount(),
                "role", RoleType.MERCHANT.name()));
        return LoginResponse.builder()
                .token(token)
                .role(RoleType.MERCHANT.name())
                .id(merchant.getMerchantId())
                .account(merchant.getMerchantAccount())
                .avatar(null)
                .build();
    }

    private LoginResponse loginAdmin(LoginRequest request) {
        SysAdmin admin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getAdminAccount, request.getAccount()));
        if (admin == null || admin.getStatus() != 1 || !request.getPassword().equals(admin.getAdminPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开发者账号或密码错误");
        }
        String token = jwtTokenManager.createToken(Map.of(
                "id", admin.getAdminId(),
                "account", admin.getAdminAccount(),
                "role", RoleType.ADMIN.name()));
        return LoginResponse.builder()
                .token(token)
                .role(RoleType.ADMIN.name())
                .id(admin.getAdminId())
                .account(admin.getAdminAccount())
                .avatar(null)
                .build();
    }

    private RoleType parseRole(String role) {
        try {
            return RoleType.valueOf(role);
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色参数非法");
        }
    }

    private void updatePassword(String oldPasswordInDb,
            PasswordChangeRequest request,
            java.util.function.Consumer<String> updateAction) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次输入新密码不一致");
        }
        if (!request.getOldPassword().equals(oldPasswordInDb)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "原密码错误");
        }
        updateAction.accept(request.getNewPassword());
    }
}
