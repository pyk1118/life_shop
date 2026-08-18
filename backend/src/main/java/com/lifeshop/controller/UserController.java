package com.lifeshop.controller;

import com.lifeshop.common.result.Result;
import com.lifeshop.dto.*;
import com.lifeshop.entity.BizAfterSale;
import com.lifeshop.entity.BizLeaveMessage;
import com.lifeshop.entity.BizMessageComment;
import com.lifeshop.entity.BizOrder;
import com.lifeshop.entity.BizUser;
import com.lifeshop.entity.BizUserAddress;
import com.lifeshop.security.LoginUser;
import com.lifeshop.security.SecurityUtils;
import com.lifeshop.service.AuthService;
import com.lifeshop.service.MerchantApplyService;
import com.lifeshop.service.UserBizService;
import com.lifeshop.vo.MerchantApplyProgressResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;
    private final MerchantApplyService merchantApplyService;
    private final UserBizService userBizService;

    @PostMapping("/merchant/apply")
    public Result<Void> applyMerchant(@Valid @RequestBody MerchantApplyRequest request) {
        merchantApplyService.apply(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @GetMapping("/merchant/progress")
    public Result<MerchantApplyProgressResponse> getProgress() {
        return Result.success(merchantApplyService.progress(SecurityUtils.getLoginUser().getId()));
    }

    @PostMapping("/password/change")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changeUserPassword(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<BizUser> profile() {
        return Result.success(userBizService.getProfile(SecurityUtils.getLoginUser().getId()));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        userBizService.updateProfile(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @GetMapping("/address/list")
    public Result<List<BizUserAddress>> addressList() {
        return Result.success(userBizService.addressList(SecurityUtils.getLoginUser().getId()));
    }

    @PostMapping("/address/save")
    public Result<Void> saveAddress(@Valid @RequestBody AddressSaveRequest request) {
        userBizService.saveAddress(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @DeleteMapping("/address/{addressId}")
    public Result<Void> deleteAddress(@PathVariable Long addressId) {
        userBizService.deleteAddress(SecurityUtils.getLoginUser().getId(), addressId);
        return Result.success();
    }

    @GetMapping("/cart/list")
    public Result<List<Map<String, Object>>> cartList() {
        return Result.success(userBizService.cartList(SecurityUtils.getLoginUser().getId()));
    }

    @PostMapping("/cart/add")
    public Result<Void> cartAdd(@Valid @RequestBody CartAddRequest request) {
        userBizService.addCart(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @PutMapping("/cart/update")
    public Result<Void> cartUpdate(@Valid @RequestBody CartUpdateRequest request) {
        userBizService.updateCart(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @DeleteMapping("/cart/{cartId}")
    public Result<Void> cartDelete(@PathVariable Long cartId) {
        userBizService.removeCart(SecurityUtils.getLoginUser().getId(), cartId);
        return Result.success();
    }

    @DeleteMapping("/cart/clear")
    public Result<Void> clearCart() {
        userBizService.clearCart(SecurityUtils.getLoginUser().getId());
        return Result.success();
    }

    @PostMapping("/order/submit")
    public Result<BizOrder> submitOrder(@Valid @RequestBody OrderSubmitRequest request) {
        return Result.success(userBizService.submitOrder(SecurityUtils.getLoginUser().getId(), request));
    }

    @GetMapping("/order/list")
    public Result<?> orderList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo) {
        return Result.success(
                userBizService.orderPage(SecurityUtils.getLoginUser().getId(), current, size, status, orderNo));
    }

    @GetMapping("/order/{orderId}")
    public Result<Map<String, Object>> orderDetail(@PathVariable Long orderId) {
        return Result.success(userBizService.orderDetail(SecurityUtils.getLoginUser().getId(), orderId));
    }

    @PostMapping("/order/{orderId}/pay")
    public Result<Void> payOrder(@PathVariable Long orderId, @RequestParam(required = false) String payType) {
        userBizService.payOrder(SecurityUtils.getLoginUser().getId(), orderId, payType);
        return Result.success();
    }

    @PostMapping("/order/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        userBizService.cancelOrder(SecurityUtils.getLoginUser().getId(), orderId);
        return Result.success();
    }

    @PostMapping("/order/{orderId}/confirm-receive")
    public Result<Void> confirmReceive(@PathVariable Long orderId) {
        userBizService.confirmReceive(SecurityUtils.getLoginUser().getId(), orderId);
        return Result.success();
    }

    @PostMapping("/order/{orderId}/refund")
    public Result<Void> applyRefund(@PathVariable Long orderId) {
        userBizService.applyRefund(SecurityUtils.getLoginUser().getId(), orderId);
        return Result.success();
    }

    @DeleteMapping("/order/{orderId}")
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        userBizService.deleteOrder(SecurityUtils.getLoginUser().getId(), orderId);
        return Result.success();
    }

    @PostMapping("/after-sale/apply")
    public Result<Void> applyAfterSale(@Valid @RequestBody AfterSaleApplyRequest request) {
        userBizService.applyAfterSale(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @GetMapping("/after-sale/list")
    public Result<List<BizAfterSale>> afterSaleList() {
        return Result.success(userBizService.afterSaleList(SecurityUtils.getLoginUser().getId()));
    }

    @PostMapping("/after-sale/{afterSaleId}/escalate")
    public Result<Void> escalateAfterSale(@PathVariable Long afterSaleId) {
        userBizService.escalateAfterSale(SecurityUtils.getLoginUser().getId(), afterSaleId);
        return Result.success();
    }

    @PostMapping("/message")
    public Result<Void> leaveMessage(@Valid @RequestBody LeaveMessageRequest request) {
        var loginUser = SecurityUtils.getLoginUser();
        userBizService.leaveMessage(loginUser.getId(), loginUser.getAccount(), request);
        return Result.success();
    }

    @GetMapping("/message/list")
    public Result<List<BizLeaveMessage>> messageList(@RequestParam(required = false) String sort) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            userId = loginUser.getId();
            userBizService.syncUserAvatar(userId);
        }
        return Result.success(userBizService.messageList(userId, sort));
    }

    @DeleteMapping("/message/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        userBizService.deleteMessage(SecurityUtils.getLoginUser().getId(), messageId);
        return Result.success();
    }

    @PostMapping("/message/comment")
    public Result<Void> addComment(@Valid @RequestBody MessageReplyRequest request) {
        var loginUser = SecurityUtils.getLoginUser();
        userBizService.addComment(loginUser.getId(), loginUser.getAccount(), 
            userBizService.getProfile(loginUser.getId()).getUserAvatar(), 
            request.getMessageId(), request.getReplyContent());
        return Result.success();
    }

    @GetMapping("/comment/list")
    public Result<List<BizMessageComment>> commentList() {
        return Result.success(userBizService.userCommentList(SecurityUtils.getLoginUser().getId()));
    }

    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        userBizService.deleteComment(SecurityUtils.getLoginUser().getId(), commentId);
        return Result.success();
    }
}
