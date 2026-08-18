package com.lifeshop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeshop.dto.*;
import com.lifeshop.entity.*;

import java.util.List;
import java.util.Map;

public interface UserBizService {
    List<BizCategory> categoryList();

    Page<BizGoods> goodsPage(long current, long size, String keyword, Long categoryId, String sort);

    Map<String, Object> goodsDetail(Long goodsId);

    List<?> shopList();

    BizUser getProfile(Long userId);

    void updateProfile(Long userId, ProfileUpdateRequest request);

    List<BizUserAddress> addressList(Long userId);

    void saveAddress(Long userId, AddressSaveRequest request);

    void deleteAddress(Long userId, Long addressId);

    List<Map<String, Object>> cartList(Long userId);

    void addCart(Long userId, CartAddRequest request);

    void updateCart(Long userId, CartUpdateRequest request);

    void removeCart(Long userId, Long cartId);

    void clearCart(Long userId);

    BizOrder submitOrder(Long userId, OrderSubmitRequest request);

    Page<BizOrder> orderPage(Long userId, long current, long size, Integer status, String orderNo);

    Map<String, Object> orderDetail(Long userId, Long orderId);

    void payOrder(Long userId, Long orderId, String payType);

    void cancelOrder(Long userId, Long orderId);

    void confirmReceive(Long userId, Long orderId);

    void applyRefund(Long userId, Long orderId);

    void deleteOrder(Long userId, Long orderId);

    void applyAfterSale(Long userId, AfterSaleApplyRequest request);

    List<BizAfterSale> afterSaleList(Long userId);

    void escalateAfterSale(Long userId, Long afterSaleId);

    void leaveMessage(Long userId, String userAccount, LeaveMessageRequest request);

    List<BizLeaveMessage> messageList(Long userId, String sort);

    void deleteMessage(Long userId, Long messageId);

    List<BizMessageComment> commentList(Long messageId);

    void addComment(Long userId, String userAccount, String userAvatar, Long messageId, String commentContent);

    List<BizMessageComment> userCommentList(Long userId);

    void deleteComment(Long userId, Long commentId);

    void syncUserAvatar(Long userId);
}
