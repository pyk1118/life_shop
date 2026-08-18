package com.lifeshop.controller;

import com.lifeshop.common.exception.BusinessException;
import com.lifeshop.common.result.Result;
import com.lifeshop.common.result.ResultCode;
import com.lifeshop.common.enums.OrderStatus;
import com.lifeshop.dto.CategorySaveRequest;
import com.lifeshop.dto.MessageReplyRequest;
import com.lifeshop.dto.PasswordChangeRequest;
import com.lifeshop.entity.*;
import com.lifeshop.mapper.BizAfterSaleMapper;
import com.lifeshop.mapper.BizGoodsMapper;
import com.lifeshop.mapper.BizOrderItemMapper;
import com.lifeshop.mapper.BizOrderMapper;
import com.lifeshop.service.AdminService;
import com.lifeshop.service.AuthService;
import com.lifeshop.vo.AdminMerchantDetailVO;
import com.lifeshop.vo.AdminOrderDetailVO;
import com.lifeshop.vo.AdminUserDetailVO;
import com.lifeshop.vo.DashboardVO;
import com.lifeshop.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;
    private final BizAfterSaleMapper bizAfterSaleMapper;
    private final BizOrderMapper bizOrderMapper;
    private final BizOrderItemMapper bizOrderItemMapper;
    private final BizGoodsMapper bizGoodsMapper;

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(adminService.dashboard());
    }

    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String phone) {
        return Result.success(adminService.userPage(current, size, account, phone));
    }

    @GetMapping("/user/{userId}")
    public Result<AdminUserDetailVO> userDetail(@PathVariable Long userId) {
        return Result.success(adminService.userDetail(userId));
    }

    @PostMapping("/user/{userId}/toggle-status")
    public Result<Void> toggleUserStatus(@PathVariable Long userId) {
        adminService.toggleUserStatus(userId);
        return Result.success();
    }

    @GetMapping("/merchant/audit/list")
    public Result<?> merchantAuditList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(adminService.merchantAuditPage(current, size, auditStatus));
    }

    @PostMapping("/merchant/{merchantId}/audit")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> auditMerchant(@PathVariable Long merchantId,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false, defaultValue = "") String auditRemark) {
        if (auditStatus != 1 && auditStatus != 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "审核状态仅支持1(通过)或2(驳回)");
        }
        adminService.auditMerchant(merchantId, auditStatus, auditRemark);
        return Result.success();
    }

    @GetMapping("/merchant/list")
    public Result<?> merchantList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String merchantAccount,
            @RequestParam(required = false) Integer merchantStatus) {
        return Result.success(adminService.merchantPage(current, size, merchantAccount, merchantStatus));
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<AdminMerchantDetailVO> merchantDetail(@PathVariable Long merchantId) {
        return Result.success(adminService.merchantDetail(merchantId));
    }

    @PostMapping("/merchant/{merchantId}/toggle-status")
    public Result<Void> toggleMerchantStatus(@PathVariable Long merchantId) {
        adminService.toggleMerchantStatus(merchantId);
        return Result.success();
    }

    @PostMapping("/merchant/{merchantId}/reset-password")
    public Result<Void> resetMerchantPassword(@PathVariable Long merchantId) {
        adminService.resetMerchantPassword(merchantId);
        return Result.success();
    }

    @GetMapping("/shop/list")
    public Result<?> shopList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String shopName,
            @RequestParam(required = false) Integer shopStatus) {
        return Result.success(adminService.shopPage(current, size, shopName, shopStatus));
    }

    @PostMapping("/shop/{shopId}/toggle-status")
    public Result<Void> toggleShopStatus(@PathVariable Long shopId) {
        adminService.toggleShopStatus(shopId);
        return Result.success();
    }

    @GetMapping("/category/list")
    public Result<?> categoryList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(adminService.categoryPage(current, size));
    }

    @GetMapping("/category/all")
    public Result<List<BizCategory>> categoryAll() {
        return Result.success(adminService.categoryAll());
    }

    @PostMapping("/category/save")
    public Result<Void> saveCategory(@Valid @RequestBody CategorySaveRequest request) {
        adminService.saveCategory(request);
        return Result.success();
    }

    @DeleteMapping("/category/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        adminService.deleteCategory(categoryId);
        return Result.success();
    }

    @GetMapping("/goods/list")
    public Result<?> goodsList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.goodsPage(current, size, keyword, categoryId, shopId, status));
    }

    @GetMapping("/goods/{goodsId}")
    public Result<BizGoods> goodsDetail(@PathVariable Long goodsId) {
        return Result.success(adminService.goodsDetail(goodsId));
    }

    @PostMapping("/goods/{goodsId}/toggle-status")
    public Result<Void> toggleGoodsStatus(@PathVariable Long goodsId) {
        adminService.toggleGoodsStatus(goodsId);
        return Result.success();
    }

    @DeleteMapping("/goods/{goodsId}")
    public Result<Void> deleteGoods(@PathVariable Long goodsId) {
        adminService.deleteGoods(goodsId);
        return Result.success();
    }

    @GetMapping("/order/list")
    public Result<?> orderList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.orderPage(current, size, orderNo, userId, shopId, status));
    }

    @GetMapping("/order/{orderId}")
    public Result<AdminOrderDetailVO> orderDetail(@PathVariable Long orderId) {
        return Result.success(adminService.orderDetail(orderId));
    }

    @GetMapping("/message/list")
    public Result<?> messageList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String userAccount,
            @RequestParam(required = false) String title) {
        return Result.success(adminService.messagePage(current, size, userAccount, title));
    }

    @PostMapping("/message/{messageId}/reply")
    public Result<Void> replyMessage(@PathVariable Long messageId,
            @Valid @RequestBody MessageReplyRequest request) {
        adminService.replyMessage(messageId, request);
        return Result.success();
    }

    @DeleteMapping("/message/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        adminService.deleteMessage(messageId);
        return Result.success();
    }

    @PostMapping("/password/change")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changeAdminPassword(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @GetMapping("/admin/list")
    public Result<?> adminList(@RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String adminAccount,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.adminPage(current, size, adminAccount, status));
    }

    @PostMapping("/admin/add")
    public Result<Void> addAdmin(@RequestBody SysAdmin admin) {
        String currentAccount = SecurityUtils.getLoginUser().getAccount();
        if (!"admin".equals(currentAccount)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅超级管理员可执行此操作");
        }
        adminService.addAdmin(admin);
        return Result.success();
    }

    @PostMapping("/admin/{adminId}/toggle-status")
    public Result<Void> toggleAdminStatus(@PathVariable Long adminId) {
        String currentAccount = SecurityUtils.getLoginUser().getAccount();
        if (!"admin".equals(currentAccount)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅超级管理员可执行此操作");
        }
        adminService.toggleAdminStatus(adminId);
        return Result.success();
    }

    @PostMapping("/admin/{adminId}/reset-password")
    public Result<Void> resetAdminPassword(@PathVariable Long adminId) {
        String currentAccount = SecurityUtils.getLoginUser().getAccount();
        if (!"admin".equals(currentAccount)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅超级管理员可执行此操作");
        }
        adminService.resetAdminPassword(adminId);
        return Result.success();
    }

    @DeleteMapping("/admin/{adminId}")
    public Result<Void> deleteAdmin(@PathVariable Long adminId) {
        String currentAccount = SecurityUtils.getLoginUser().getAccount();
        if (!"admin".equals(currentAccount)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅超级管理员可执行此操作");
        }
        adminService.deleteAdmin(adminId);
        return Result.success();
    }

    @GetMapping("/after-sale/list")
    public Result<List<BizAfterSale>> getAfterSaleList() {
        try {
            List<BizAfterSale> afterSales = bizAfterSaleMapper.selectList(new LambdaQueryWrapper<BizAfterSale>()
                    .eq(BizAfterSale::getEscalated, 1)
                    .orderByDesc(BizAfterSale::getAfterSaleId));
            for (BizAfterSale as : afterSales) {
                if (as.getOrderId() != null) {
                    BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(as.getOrderId());
                    if (order != null) {
                        as.setOrderNo(order.getOrderNo());
                    }
                }
                if (as.getIsDeleted() == null) {
                    as.setIsDeleted(0);
                }
            }
            return Result.success(afterSales);
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }

    @PostMapping("/after-sale/{afterSaleId}/handle")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> handleAfterSale(@PathVariable Long afterSaleId,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false, defaultValue = "") String adminRemark) {
        BizAfterSale afterSale = bizAfterSaleMapper.selectById(afterSaleId);
        if (afterSale == null || !Integer.valueOf(1).equals(afterSale.getEscalated())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "售后记录不存在或未申请平台介入");
        }
        afterSale.setAuditStatus(auditStatus);
        afterSale.setAdminRemark(adminRemark);
        afterSale.setIsDeleted(afterSale.getIsDeleted() != null ? afterSale.getIsDeleted() : 0);
        bizAfterSaleMapper.updateById(afterSale);

        BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(afterSale.getOrderId());
        if (order != null) {
            if (auditStatus == 1) {
                order.setOrderStatus(OrderStatus.REFUNDED);
                bizOrderMapper.updateById(order);
            } else if (auditStatus == 2) {
                order.setOrderStatus(OrderStatus.FINISHED);
                order.setReceiveTime(java.time.LocalDateTime.now());
                bizOrderMapper.updateById(order);
                List<BizOrderItem> items = bizOrderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
                        .eq(BizOrderItem::getOrderId, order.getOrderId()));
                for (BizOrderItem item : items) {
                    BizGoods goods = bizGoodsMapper.selectById(item.getGoodsId());
                    if (goods != null) {
                        bizGoodsMapper.deductStock(goods.getGoodsId(), item.getGoodsNum());
                        bizGoodsMapper.incrementSalesCount(goods.getGoodsId(), item.getGoodsNum());
                    }
                }
            }
        }
        return Result.success();
    }
}
