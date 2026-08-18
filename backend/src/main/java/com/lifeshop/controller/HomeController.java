package com.lifeshop.controller;

import com.lifeshop.common.result.Result;
import com.lifeshop.dto.LoginRequest;
import com.lifeshop.dto.UserRegisterRequest;
import com.lifeshop.entity.BizCategory;
import com.lifeshop.entity.BizGoods;
import com.lifeshop.security.LoginUser;
import com.lifeshop.service.AuthService;
import com.lifeshop.service.UserBizService;
import com.lifeshop.vo.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class HomeController {

    private final AuthService authService;
    private final UserBizService userBizService;

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/auth/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterRequest request) {
        authService.registerUser(request);
        return Result.success();
    }

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("life-shop-backend is running");
    }

    @GetMapping("/categories")
    public Result<List<BizCategory>> categories() {
        return Result.success(userBizService.categoryList());
    }

    @GetMapping("/goods")
    public Result<?> goods(@RequestParam(defaultValue = "1") long current,
                           @RequestParam(defaultValue = "10") long size,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) String sort) {
        return Result.success(userBizService.goodsPage(current, size, keyword, categoryId, sort));
    }

    @GetMapping("/goods/{goodsId}")
    public Result<Map<String, Object>> goodsDetail(@PathVariable Long goodsId) {
        return Result.success(userBizService.goodsDetail(goodsId));
    }

    @GetMapping("/shops")
    public Result<?> shops() {
        return Result.success(userBizService.shopList());
    }

    @GetMapping("/messages")
    public Result<?> messages(@RequestParam(required = false) String sort) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            userBizService.syncUserAvatar(loginUser.getId());
        }
        return Result.success(userBizService.messageList(null, sort));
    }

    @GetMapping("/message/comments/{messageId}")
    public Result<?> messageComments(@PathVariable Long messageId) {
        return Result.success(userBizService.commentList(messageId));
    }
}
