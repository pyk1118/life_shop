package com.lifeshop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeshop.common.result.Result;
import com.lifeshop.common.enums.OrderStatus;
import com.lifeshop.common.exception.BusinessException;
import com.lifeshop.common.result.ResultCode;
import com.lifeshop.dto.PasswordChangeRequest;
import com.lifeshop.entity.BizAfterSale;
import com.lifeshop.entity.BizCategory;
import com.lifeshop.entity.BizGoods;
import com.lifeshop.entity.BizOrder;
import com.lifeshop.entity.BizOrderItem;
import com.lifeshop.entity.BizShop;
import com.lifeshop.entity.BizLeaveMessage;
import com.lifeshop.mapper.BizAfterSaleMapper;
import com.lifeshop.mapper.BizCategoryMapper;
import com.lifeshop.mapper.BizGoodsMapper;
import com.lifeshop.mapper.BizOrderMapper;
import com.lifeshop.mapper.BizOrderItemMapper;
import com.lifeshop.mapper.BizShopMapper;
import com.lifeshop.mapper.BizLeaveMessageMapper;
import com.lifeshop.mapper.BizMessageCommentMapper;
import com.lifeshop.mapper.BizUserMapper;
import com.lifeshop.entity.BizUser;
import com.lifeshop.security.SecurityUtils;
import com.lifeshop.service.AuthService;
import com.lifeshop.service.EmailService;
import com.lifeshop.dto.MessageReplyRequest;
import com.lifeshop.entity.BizMessageComment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant")
public class MerchantController {
    private final AuthService authService;
    private final BizOrderMapper bizOrderMapper;
    private final BizOrderItemMapper bizOrderItemMapper;
    private final BizGoodsMapper bizGoodsMapper;
    private final BizShopMapper bizShopMapper;
    private final BizAfterSaleMapper bizAfterSaleMapper;
    private final BizCategoryMapper bizCategoryMapper;
    private final BizLeaveMessageMapper bizLeaveMessageMapper;
    private final BizMessageCommentMapper bizMessageCommentMapper;
    private final BizUserMapper bizUserMapper;
    private final EmailService emailService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        
        Long goodsCount = bizGoodsMapper.selectCount(new LambdaQueryWrapper<BizGoods>()
                .eq(BizGoods::getMerchantId, merchantId));
        
        Long orderCount = bizOrderMapper.countAllByMerchantId(merchantId);
        
        Long pendingDelivery = bizOrderMapper.countByMerchantIdAndStatus(merchantId, OrderStatus.WAIT_DELIVER);
        
        Long pendingAfterSale = bizAfterSaleMapper.selectCount(new LambdaQueryWrapper<BizAfterSale>()
                .eq(BizAfterSale::getMerchantId, merchantId)
                .eq(BizAfterSale::getAuditStatus, 0)
                .eq(BizAfterSale::getEscalated, 0));
        
        BizShop shop = bizShopMapper.selectOne(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getMerchantId, merchantId));
        Long pendingMessage = 0L;
        if (shop != null) {
            pendingMessage = bizLeaveMessageMapper.countUnrepliedByShopName(shop.getShopName());
        }
        
        java.util.List<BizOrder> paidOrders = bizOrderMapper.selectPaidOrdersByMerchantId(merchantId);
        java.math.BigDecimal totalAmount = paidOrders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        List<BizGoods> allGoods = bizGoodsMapper.selectList(new LambdaQueryWrapper<BizGoods>()
                .eq(BizGoods::getMerchantId, merchantId));
        java.util.Map<Long, Long> categoryCount = new java.util.LinkedHashMap<>();
        java.util.Map<Long, java.math.BigDecimal> categorySales = new java.util.LinkedHashMap<>();
        for (BizGoods g : allGoods) {
            Long catId = g.getCategoryId() != null ? g.getCategoryId() : 0L;
            categoryCount.merge(catId, 1L, Long::sum);
            java.math.BigDecimal sales = g.getSalePrice() != null && g.getSalesCount() != null
                    ? g.getSalePrice().multiply(java.math.BigDecimal.valueOf(g.getSalesCount()))
                    : java.math.BigDecimal.ZERO;
            categorySales.merge(catId, sales, java.math.BigDecimal::add);
        }
        java.util.Map<Long, String> categoryNameMap = new java.util.HashMap<>();
        for (Long catId : categoryCount.keySet()) {
            if (catId > 0) {
                BizCategory cat = bizCategoryMapper.selectById(catId);
                categoryNameMap.put(catId, cat != null ? cat.getCategoryName() : "未知分类");
            } else {
                categoryNameMap.put(catId, "未分类");
            }
        }
        java.util.List<java.util.Map<String, Object>> categoryStats = new java.util.ArrayList<>();
        for (Long catId : categoryCount.keySet()) {
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("categoryId", catId);
            item.put("categoryName", categoryNameMap.getOrDefault(catId, "未知"));
            item.put("goodsCount", categoryCount.get(catId));
            item.put("salesAmount", categorySales.getOrDefault(catId, java.math.BigDecimal.ZERO));
            categoryStats.add(item);
        }
        
        java.util.Map<String, java.math.BigDecimal> monthlySales = new java.util.TreeMap<>();
        java.util.Map<String, java.math.BigDecimal> dailySales = new java.util.TreeMap<>();
        java.time.format.DateTimeFormatter monthFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        java.time.format.DateTimeFormatter dayFmt = java.time.format.DateTimeFormatter.ofPattern("MM-dd");
        for (BizOrder o : paidOrders) {
            if (o.getPayTime() != null && o.getTotalAmount() != null) {
                String month = o.getPayTime().format(monthFmt);
                String day = o.getPayTime().format(dayFmt);
                monthlySales.merge(month, o.getTotalAmount(), java.math.BigDecimal::add);
                dailySales.merge(day, o.getTotalAmount(), java.math.BigDecimal::add);
            }
        }
        java.util.List<java.util.Map<String, Object>> monthlySalesList = new java.util.ArrayList<>();
        for (var entry : monthlySales.entrySet()) {
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("month", entry.getKey());
            item.put("sales", entry.getValue());
            monthlySalesList.add(item);
        }
        java.util.List<java.util.Map<String, Object>> dailySalesList = new java.util.ArrayList<>();
        for (var entry : dailySales.entrySet()) {
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("date", entry.getKey());
            item.put("sales", entry.getValue());
            dailySalesList.add(item);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("goodsCount", goodsCount);
        data.put("orderCount", orderCount);
        data.put("totalAmount", totalAmount);
        data.put("pendingDelivery", pendingDelivery);
        data.put("pendingAfterSale", pendingAfterSale);
        data.put("pendingMessage", pendingMessage);
        data.put("categoryStats", categoryStats);
        data.put("monthlySales", monthlySalesList);
        data.put("dailySales", dailySalesList);
        
        return Result.success(data);
    }

    @GetMapping("/shop")
    public Result<BizShop> getShopInfo() {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        BizShop shop = bizShopMapper.selectOne(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getMerchantId, merchantId));
        if (shop == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "店铺信息不存在");
        }
        return Result.success(shop);
    }

    @PutMapping("/shop")
    public Result<Void> updateShopInfo(@RequestBody BizShop shop) {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        BizShop existingShop = bizShopMapper.selectOne(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getMerchantId, merchantId));
        if (existingShop == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "店铺信息不存在");
        }
        existingShop.setShopName(shop.getShopName());
        existingShop.setShopDesc(shop.getShopDesc());
        existingShop.setBusinessHours(shop.getBusinessHours());
        existingShop.setShopLogo(shop.getShopLogo());
        bizShopMapper.updateById(existingShop);
        return Result.success();
    }

    @GetMapping("/goods/list")
    public Result<List<BizGoods>> getGoodsList() {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        List<BizGoods> goods = bizGoodsMapper.selectList(new LambdaQueryWrapper<BizGoods>()
                .eq(BizGoods::getMerchantId, merchantId)
                .orderByDesc(BizGoods::getGoodsId));
        return Result.success(goods);
    }

    @PostMapping("/goods/save")
    public Result<Void> saveGoods(@RequestBody BizGoods goods) {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        if (goods.getGoodsId() == null) {
            goods.setMerchantId(merchantId);
            goods.setShopId(merchantId);
            goods.setStatus(0);
            bizGoodsMapper.insert(goods);
        } else {
            BizGoods existing = bizGoodsMapper.selectById(goods.getGoodsId());
            if (existing == null || !Objects.equals(existing.getMerchantId(), merchantId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在");
            }
            goods.setMerchantId(merchantId);
            goods.setShopId(merchantId);
            bizGoodsMapper.updateById(goods);
        }
        return Result.success();
    }

    @PostMapping("/goods/{goodsId}/toggle-status")
    public Result<Void> toggleGoodsStatus(@PathVariable Long goodsId) {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        BizGoods goods = bizGoodsMapper.selectById(goodsId);
        if (goods == null || !Objects.equals(goods.getMerchantId(), merchantId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在");
        }
        goods.setStatus(goods.getStatus() == 1 ? 0 : 1);
        bizGoodsMapper.updateById(goods);
        return Result.success();
    }

    @GetMapping("/order/list")
    public Result<List<BizOrder>> getOrderList() {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        List<BizOrder> orders = bizOrderMapper.selectAllByMerchantId(merchantId);
        return Result.success(orders);
    }

    @GetMapping("/order/{orderId}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(orderId);
        if (order == null || !Objects.equals(order.getMerchantId(), merchantId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单不存在");
        }
        List<BizOrderItem> items = bizOrderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
                .eq(BizOrderItem::getOrderId, orderId));
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("orderStatus", order.getOrderStatus());
        data.put("totalAmount", order.getTotalAmount());
        data.put("receiverName", order.getReceiverName());
        data.put("receiverPhone", order.getReceiverPhone());
        data.put("receiverAddress", order.getReceiverAddress());
        data.put("logisticsNo", order.getLogisticsNo());
        data.put("items", items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("goodsName", item.getGoodsName());
            m.put("goodsPrice", item.getGoodsPrice());
            m.put("goodsNum", item.getGoodsNum());
            m.put("totalPrice", item.getTotalPrice());
            return m;
        }).collect(java.util.stream.Collectors.toList()));
        return Result.success(data);
    }

    @PostMapping("/order/{orderId}/deliver")
    public Result<Void> deliverOrder(@PathVariable Long orderId, @RequestParam(required = false) String logisticsNo) {
        BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(orderId);
        if (order == null || !Objects.equals(order.getMerchantId(), SecurityUtils.getLoginUser().getId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.WAIT_DELIVER) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅待发货订单可发货");
        }
        order.setOrderStatus(OrderStatus.WAIT_RECEIVE);
        order.setLogisticsNo(logisticsNo == null ? "" : logisticsNo);
        order.setDeliverTime(LocalDateTime.now());
        bizOrderMapper.updateById(order);
        return Result.success();
    }

    @GetMapping("/after-sale/list")
    public Result<List<BizAfterSale>> getAfterSaleList() {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        List<BizAfterSale> afterSales = bizAfterSaleMapper.selectList(new LambdaQueryWrapper<BizAfterSale>()
                .eq(BizAfterSale::getMerchantId, merchantId)
                .eq(BizAfterSale::getEscalated, 0)
                .orderByDesc(BizAfterSale::getAfterSaleId));
        for (BizAfterSale as : afterSales) {
            if (as.getOrderId() != null) {
                BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(as.getOrderId());
                if (order != null) {
                    as.setOrderNo(order.getOrderNo());
                }
            }
        }
        return Result.success(afterSales);
    }

    @PostMapping("/after-sale/{afterSaleId}/handle")
    public Result<Void> handleAfterSale(@PathVariable Long afterSaleId,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false, defaultValue = "") String auditRemark) {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        BizAfterSale afterSale = bizAfterSaleMapper.selectById(afterSaleId);
        if (afterSale == null || !Objects.equals(afterSale.getMerchantId(), merchantId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "售后记录不存在");
        }
        if (afterSale.getEscalated() != null && afterSale.getEscalated() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该售后已申请平台介入，请等待开发者处理");
        }
        afterSale.setAuditStatus(auditStatus);
        String defaultRemark = auditStatus == 1 ? "商家同意退款" : "商家驳回";
        afterSale.setAuditRemark(auditRemark != null && !auditRemark.isBlank() ? auditRemark : defaultRemark);
        bizAfterSaleMapper.updateById(afterSale);

        if (auditStatus == 1) {
            BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(afterSale.getOrderId());
            if (order != null) {
                order.setOrderStatus(OrderStatus.REFUNDED);
                bizOrderMapper.updateById(order);
            }
        } else if (auditStatus == 2) {
            BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(afterSale.getOrderId());
            if (order != null && order.getOrderStatus() == OrderStatus.REFUNDING) {
                order.setOrderStatus(OrderStatus.REFUNDING);
                bizOrderMapper.updateById(order);
            }
        }

        return Result.success();
    }

    @PostMapping("/password/change")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changeMerchantPassword(SecurityUtils.getLoginUser().getId(), request);
        return Result.success();
    }

    @GetMapping("/message/list")
    public Result<List<BizLeaveMessage>> getMessageList() {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        BizShop shop = bizShopMapper.selectOne(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getMerchantId, merchantId));
        if (shop == null) {
            return Result.success(List.of());
        }
        List<BizLeaveMessage> messages = bizLeaveMessageMapper.selectList(new LambdaQueryWrapper<BizLeaveMessage>()
                .like(BizLeaveMessage::getMessageTitle, "@" + shop.getShopName())
                .orderByDesc(BizLeaveMessage::getMessageId));
        return Result.success(messages);
    }

    @PostMapping("/message/{messageId}/reply")
    public Result<Void> replyMessage(@PathVariable Long messageId, @Valid @RequestBody MessageReplyRequest request) {
        Long merchantId = SecurityUtils.getLoginUser().getId();
        BizShop shop = bizShopMapper.selectOne(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getMerchantId, merchantId));
        if (shop == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "店铺不存在");
        }
        BizLeaveMessage message = bizLeaveMessageMapper.selectById(messageId);
        if (message == null || !message.getMessageTitle().contains("@" + shop.getShopName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "留言不存在");
        }
        message.setReplyContent(request.getReplyContent());
        bizLeaveMessageMapper.updateById(message);
        BizMessageComment existingComment = bizMessageCommentMapper.selectOne(new LambdaQueryWrapper<BizMessageComment>()
                .eq(BizMessageComment::getMessageId, messageId)
                .eq(BizMessageComment::getUserId, -merchantId));
        if (existingComment != null) {
            existingComment.setCommentContent(request.getReplyContent());
            bizMessageCommentMapper.updateById(existingComment);
        } else {
            BizMessageComment comment = new BizMessageComment();
            comment.setMessageId(messageId);
            comment.setUserId(-merchantId);
            comment.setUserAccount(shop.getShopName());
            comment.setUserAvatar(shop.getShopLogo() != null ? shop.getShopLogo() : "");
            comment.setCommentContent(request.getReplyContent());
            comment.setIsTop(2);
            bizMessageCommentMapper.insert(comment);
        }
        if (message.getUserId() != null) {
            BizUser msgUser = bizUserMapper.selectById(message.getUserId());
            if (msgUser != null && msgUser.getEmail() != null) {
                emailService.sendMerchantReplyNotification(msgUser.getEmail(), shop.getShopName(), message.getMessageTitle(), message.getMessageContent(), request.getReplyContent());
            }
        }
        return Result.success();
    }
}
