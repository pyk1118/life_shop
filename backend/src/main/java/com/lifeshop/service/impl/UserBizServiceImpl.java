package com.lifeshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeshop.common.enums.OrderStatus;
import com.lifeshop.common.exception.BusinessException;
import com.lifeshop.common.result.ResultCode;
import com.lifeshop.dto.*;
import com.lifeshop.entity.*;
import com.lifeshop.mapper.*;
import com.lifeshop.service.UserBizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserBizServiceImpl implements UserBizService {
    private final BizCategoryMapper bizCategoryMapper;
    private final BizGoodsMapper bizGoodsMapper;
    private final BizUserMapper bizUserMapper;
    private final BizUserAddressMapper bizUserAddressMapper;
    private final BizCartMapper bizCartMapper;
    private final BizOrderMapper bizOrderMapper;
    private final BizOrderItemMapper bizOrderItemMapper;
    private final BizAfterSaleMapper bizAfterSaleMapper;
    private final BizLeaveMessageMapper bizLeaveMessageMapper;
    private final BizShopMapper bizShopMapper;
    private final BizMessageCommentMapper bizMessageCommentMapper;

    @Override
    public List<BizCategory> categoryList() {
        return bizCategoryMapper.selectList(new LambdaQueryWrapper<BizCategory>()
                .eq(BizCategory::getStatus, 1)
                .orderByAsc(BizCategory::getSort));
    }

    @Override
    public Page<BizGoods> goodsPage(long current, long size, String keyword, Long categoryId, String sort) {
        LambdaQueryWrapper<BizGoods> qw = new LambdaQueryWrapper<BizGoods>()
                .eq(BizGoods::getStatus, 1)
                .like(keyword != null && !keyword.isBlank(), BizGoods::getGoodsName, keyword)
                .eq(categoryId != null, BizGoods::getCategoryId, categoryId);
        if ("salePriceAsc".equals(sort)) {
            qw.orderByAsc(BizGoods::getSalePrice);
        } else if ("salePriceDesc".equals(sort)) {
            qw.orderByDesc(BizGoods::getSalePrice);
        } else if ("salesCountAsc".equals(sort)) {
            qw.orderByAsc(BizGoods::getSalesCount);
            qw.orderByDesc(BizGoods::getGoodsId);
        } else if ("salesCountDesc".equals(sort)) {
            qw.orderByDesc(BizGoods::getSalesCount);
            qw.orderByDesc(BizGoods::getGoodsId);
        } else {
            qw.orderByDesc(BizGoods::getGoodsId);
        }
        Page<BizGoods> page = bizGoodsMapper.selectPage(new Page<>(current, size), qw);
        page.getRecords().removeIf(goods -> {
            if (goods.getShopId() != null) {
                BizShop shop = bizShopMapper.selectById(goods.getShopId());
                return shop == null || !Integer.valueOf(1).equals(shop.getShopStatus());
            }
            return false;
        });
        for (BizGoods goods : page.getRecords()) {
            if (goods.getCategoryId() != null) {
                BizCategory category = bizCategoryMapper.selectById(goods.getCategoryId());
                if (category != null) {
                    goods.setCategoryName(category.getCategoryName());
                }
            }
        }
        return page;
    }

    @Override
    public Map<String, Object> goodsDetail(Long goodsId) {
        BizGoods goods = bizGoodsMapper.selectById(goodsId);
        if (goods == null || goods.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在或已下架");
        }
        if (goods.getShopId() != null) {
            BizShop shop = bizShopMapper.selectById(goods.getShopId());
            if (shop == null || !Integer.valueOf(1).equals(shop.getShopStatus())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在或已下架");
            }
        }
        if (goods.getCategoryId() != null) {
            BizCategory category = bizCategoryMapper.selectById(goods.getCategoryId());
            if (category != null) {
                goods.setCategoryName(category.getCategoryName());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("goods", goods);
        if (goods.getShopId() != null) {
            BizShop shop = bizShopMapper.selectById(goods.getShopId());
            result.put("shop", shop);
            if (shop != null && shop.getMerchantId() != null) {
                BizUser merchantUser = bizUserMapper.selectById(shop.getMerchantId());
                if (merchantUser != null) {
                    result.put("merchantPhone", merchantUser.getPhone() != null ? merchantUser.getPhone() : "");
                    result.put("merchantEmail", merchantUser.getEmail() != null ? merchantUser.getEmail() : "");
                    result.put("merchantAddress", merchantUser.getAddress() != null ? merchantUser.getAddress() : "");
                }
            }
        }
        return result;
    }

    @Override
    public List<?> shopList() {
        return bizShopMapper.selectList(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getShopStatus, 1));
    }

    @Override
    public BizUser getProfile(Long userId) {
        BizUser user = bizUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        user.setUserPassword(null);
        return user;
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        BizUser user = bizUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        if (request.getUserAvatar() != null)
            user.setUserAvatar(request.getUserAvatar());
        if (request.getRealName() != null)
            user.setRealName(request.getRealName());
        if (request.getGender() != null)
            user.setGender(request.getGender());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        if (request.getEmail() != null)
            user.setEmail(request.getEmail());
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());
        bizUserMapper.updateById(user);
    }

    @Override
    public List<BizUserAddress> addressList(Long userId) {
        return bizUserAddressMapper.selectList(new LambdaQueryWrapper<BizUserAddress>()
                .eq(BizUserAddress::getUserId, userId)
                .orderByDesc(BizUserAddress::getIsDefault)
                .orderByDesc(BizUserAddress::getAddressId));
    }

    @Override
    public void saveAddress(Long userId, AddressSaveRequest request) {
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            resetDefaultAddress(userId);
        }
        if (request.getAddressId() == null) {
            BizUserAddress entity = new BizUserAddress();
            entity.setUserId(userId);
            entity.setReceiverName(request.getReceiverName());
            entity.setReceiverPhone(request.getReceiverPhone());
            entity.setReceiverAddress(request.getReceiverAddress());
            entity.setIsDefault(request.getIsDefault() == null ? 0 : request.getIsDefault());
            bizUserAddressMapper.insert(entity);
        } else {
            BizUserAddress entity = bizUserAddressMapper.selectById(request.getAddressId());
            checkAddressOwner(userId, entity);
            entity.setReceiverName(request.getReceiverName());
            entity.setReceiverPhone(request.getReceiverPhone());
            entity.setReceiverAddress(request.getReceiverAddress());
            entity.setIsDefault(request.getIsDefault() == null ? 0 : request.getIsDefault());
            bizUserAddressMapper.updateById(entity);
        }
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        BizUserAddress entity = bizUserAddressMapper.selectById(addressId);
        checkAddressOwner(userId, entity);
        bizUserAddressMapper.deleteById(addressId);
    }

    @Override
    public List<Map<String, Object>> cartList(Long userId) {
        List<BizCart> cartList = bizCartMapper.selectList(new LambdaQueryWrapper<BizCart>()
                .eq(BizCart::getUserId, userId).orderByDesc(BizCart::getCartId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (BizCart cart : cartList) {
            BizGoods goods = bizGoodsMapper.selectById(cart.getGoodsId());
            if (goods == null)
                continue;
            Map<String, Object> map = new HashMap<>();
            map.put("cartId", cart.getCartId());
            map.put("goodsId", goods.getGoodsId());
            map.put("goodsName", goods.getGoodsName());
            map.put("goodsImage", goods.getGoodsImage());
            map.put("salePrice", goods.getSalePrice());
            map.put("stock", goods.getStock());
            map.put("goodsNum", cart.getGoodsNum());
            map.put("totalPrice", goods.getSalePrice().multiply(BigDecimal.valueOf(cart.getGoodsNum())));
            result.add(map);
        }
        return result;
    }

    @Override
    public void addCart(Long userId, CartAddRequest request) {
        BizGoods goods = bizGoodsMapper.selectById(request.getGoodsId());
        if (goods == null || goods.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品不可购买");
        }
        if (goods.getStock() < request.getGoodsNum()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "库存不足");
        }
        BizCart exist = bizCartMapper.selectOne(new LambdaQueryWrapper<BizCart>()
                .eq(BizCart::getUserId, userId)
                .eq(BizCart::getGoodsId, request.getGoodsId()));
        if (exist == null) {
            BizCart cart = new BizCart();
            cart.setUserId(userId);
            cart.setGoodsId(request.getGoodsId());
            cart.setGoodsNum(request.getGoodsNum());
            bizCartMapper.insert(cart);
        } else {
            int newNum = exist.getGoodsNum() + request.getGoodsNum();
            if (newNum > goods.getStock()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "库存不足");
            }
            exist.setGoodsNum(newNum);
            bizCartMapper.updateById(exist);
        }
    }

    @Override
    public void updateCart(Long userId, CartUpdateRequest request) {
        BizCart cart = bizCartMapper.selectById(request.getCartId());
        if (cart == null || !Objects.equals(cart.getUserId(), userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "购物车项不存在");
        }
        BizGoods goods = bizGoodsMapper.selectById(cart.getGoodsId());
        if (goods == null || goods.getStock() < request.getGoodsNum()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "库存不足");
        }
        cart.setGoodsNum(request.getGoodsNum());
        bizCartMapper.updateById(cart);
    }

    @Override
    public void removeCart(Long userId, Long cartId) {
        BizCart cart = bizCartMapper.selectById(cartId);
        if (cart == null || !Objects.equals(cart.getUserId(), userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "购物车项不存在");
        }
        bizCartMapper.deleteById(cartId);
    }

    @Override
    public void clearCart(Long userId) {
        bizCartMapper.delete(new LambdaQueryWrapper<BizCart>().eq(BizCart::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizOrder submitOrder(Long userId, OrderSubmitRequest request) {
        BizUserAddress address = bizUserAddressMapper.selectById(request.getAddressId());
        checkAddressOwner(userId, address);
        List<BizCart> carts = bizCartMapper.selectBatchIds(request.getCartIds());
        if (carts.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "购物车为空");
        }
        carts.forEach(c -> {
            if (!Objects.equals(c.getUserId(), userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "禁止操作其他用户购物车");
            }
        });

        Long merchantId = null;
        Long shopId = null;
        BigDecimal total = BigDecimal.ZERO;
        List<BizOrderItem> items = new ArrayList<>();
        for (BizCart cart : carts) {
            BizGoods goods = bizGoodsMapper.selectById(cart.getGoodsId());
            if (goods == null || goods.getStatus() != 1) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "存在无效商品，无法下单");
            }
            if (merchantId == null)
                merchantId = goods.getMerchantId();
            if (shopId == null)
                shopId = goods.getShopId();
            if (!Objects.equals(merchantId, goods.getMerchantId()) || !Objects.equals(shopId, goods.getShopId())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "一次下单仅支持同一店铺商品");
            }
            if (goods.getStock() == null || goods.getStock() < cart.getGoodsNum()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "商品库存不足，请刷新后重试");
            }
            BizOrderItem item = new BizOrderItem();
            item.setGoodsId(goods.getGoodsId());
            item.setGoodsName(goods.getGoodsName());
            item.setGoodsImage(goods.getGoodsImage());
            item.setGoodsPrice(goods.getSalePrice());
            item.setGoodsNum(cart.getGoodsNum());
            item.setTotalPrice(goods.getSalePrice().multiply(BigDecimal.valueOf(cart.getGoodsNum())));
            total = total.add(item.getTotalPrice());
            items.add(item);
        }

        BizOrder order = new BizOrder();
        order.setOrderNo("LS" + System.currentTimeMillis() + new Random().nextInt(1000));
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setShopId(shopId);
        order.setTotalAmount(total);
        order.setPayType(request.getPayType() == null ? "" : request.getPayType());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getReceiverAddress());
        order.setOrderStatus(OrderStatus.WAIT_PAY);
        order.setLogisticsNo("");
        bizOrderMapper.insert(order);

        for (BizOrderItem item : items) {
            item.setOrderId(order.getOrderId());
            bizOrderItemMapper.insert(item);
        }
        for (BizCart cart : carts) {
            bizCartMapper.deleteById(cart.getCartId());
        }
        return order;
    }

    @Override
    public Page<BizOrder> orderPage(Long userId, long current, long size, Integer status, String orderNo) {
        LambdaQueryWrapper<BizOrder> qw = new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getUserId, userId)
                .eq(BizOrder::getIsDeleted, 0)
                .eq(status != null, BizOrder::getOrderStatus, status)
                .like(orderNo != null && !orderNo.isBlank(), BizOrder::getOrderNo, orderNo)
                .orderByDesc(BizOrder::getOrderId);
        return bizOrderMapper.selectPage(new Page<>(current, size), qw);
    }

    @Override
    public Map<String, Object> orderDetail(Long userId, Long orderId) {
        BizOrder order = getOwnedOrder(userId, orderId);
        List<BizOrderItem> items = bizOrderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
                .eq(BizOrderItem::getOrderId, orderId));
        Map<String, Object> map = new HashMap<>();
        map.put("order", order);
        map.put("items", items);
        return map;
    }

    @Override
    public void payOrder(Long userId, Long orderId, String payType) {
        BizOrder order = getOwnedOrder(userId, orderId);
        if (order.getOrderStatus() != OrderStatus.WAIT_PAY) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅待付款订单可支付");
        }
        order.setOrderStatus(OrderStatus.WAIT_DELIVER);
        order.setPayType(payType == null ? "ONLINE" : payType);
        order.setPayTime(LocalDateTime.now());
        bizOrderMapper.updateById(order);
    }

    @Override
    public void cancelOrder(Long userId, Long orderId) {
        BizOrder order = getOwnedOrder(userId, orderId);
        if (order.getOrderStatus() != OrderStatus.WAIT_PAY && order.getOrderStatus() != OrderStatus.WAIT_DELIVER) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不可取消订单");
        }
        order.setOrderStatus(OrderStatus.CANCELED);
        bizOrderMapper.updateById(order);
    }

    @Override
    public void confirmReceive(Long userId, Long orderId) {
        BizOrder order = getOwnedOrder(userId, orderId);
        if (order.getOrderStatus() != OrderStatus.WAIT_RECEIVE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅待收货订单可确认收货");
        }
        order.setOrderStatus(OrderStatus.FINISHED);
        order.setReceiveTime(LocalDateTime.now());
        bizOrderMapper.updateById(order);
        List<BizOrderItem> items = bizOrderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
                .eq(BizOrderItem::getOrderId, orderId));
        for (BizOrderItem item : items) {
            BizGoods goods = bizGoodsMapper.selectById(item.getGoodsId());
            if (goods != null) {
                bizGoodsMapper.deductStock(goods.getGoodsId(), item.getGoodsNum());
                bizGoodsMapper.incrementSalesCount(goods.getGoodsId(), item.getGoodsNum());
            }
        }
    }

    @Override
    public void applyRefund(Long userId, Long orderId) {
        BizOrder order = getOwnedOrder(userId, orderId);
        if (order.getOrderStatus() != OrderStatus.WAIT_RECEIVE && order.getOrderStatus() != OrderStatus.FINISHED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅待收货或已完成订单可申请售后");
        }
        order.setOrderStatus(OrderStatus.REFUNDING);
        bizOrderMapper.updateById(order);
        List<BizOrderItem> items = bizOrderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
                .eq(BizOrderItem::getOrderId, orderId));
        for (BizOrderItem item : items) {
            BizAfterSale afterSale = new BizAfterSale();
            afterSale.setOrderId(orderId);
            afterSale.setItemId(item.getItemId());
            afterSale.setUserId(userId);
            afterSale.setMerchantId(order.getMerchantId());
            afterSale.setRefundAmount(item.getTotalPrice());
            afterSale.setRefundReason("用户申请退款");
            afterSale.setRefundVoucher("");
            afterSale.setAuditStatus(0);
            afterSale.setAuditRemark("");
            afterSale.setAdminRemark("");
            afterSale.setEscalated(0);
            afterSale.setIsDeleted(0);
            bizAfterSaleMapper.insert(afterSale);
        }
    }

    @Override
    public void deleteOrder(Long userId, Long orderId) {
        BizOrder order = getOwnedOrder(userId, orderId);
        if (order.getOrderStatus() != OrderStatus.FINISHED
                && order.getOrderStatus() != OrderStatus.CANCELED
                && order.getOrderStatus() != OrderStatus.REFUNDED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅已完成、已取消或已退款订单可删除");
        }
        order.setIsDeleted(1);
        bizOrderMapper.updateById(order);
    }

    @Override
    public void applyAfterSale(Long userId, AfterSaleApplyRequest request) {
        BizOrder order = getOwnedOrder(userId, request.getOrderId());
        if (order.getOrderStatus() != OrderStatus.WAIT_RECEIVE && order.getOrderStatus() != OrderStatus.FINISHED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅待收货或已完成订单可申请售后");
        }
        BizOrderItem item = bizOrderItemMapper.selectById(request.getItemId());
        if (item == null || !Objects.equals(item.getOrderId(), order.getOrderId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "售后商品不合法");
        }
        BizAfterSale afterSale = new BizAfterSale();
        afterSale.setOrderId(order.getOrderId());
        afterSale.setItemId(item.getItemId());
        afterSale.setUserId(userId);
        afterSale.setMerchantId(order.getMerchantId());
        afterSale.setRefundAmount(request.getRefundAmount());
        afterSale.setRefundReason(request.getRefundReason());
        afterSale.setRefundVoucher(request.getRefundVoucher() == null ? "" : request.getRefundVoucher());
        afterSale.setAuditStatus(0);
        afterSale.setAuditRemark("");
        afterSale.setAdminRemark("");
        afterSale.setEscalated(0);
        afterSale.setIsDeleted(0);
        bizAfterSaleMapper.insert(afterSale);
        order.setOrderStatus(OrderStatus.REFUNDING);
        bizOrderMapper.updateById(order);
    }

    @Override
    public List<BizAfterSale> afterSaleList(Long userId) {
        List<BizAfterSale> list = bizAfterSaleMapper.selectList(new LambdaQueryWrapper<BizAfterSale>()
                .eq(BizAfterSale::getUserId, userId)
                .orderByDesc(BizAfterSale::getAfterSaleId));
        for (BizAfterSale as : list) {
            BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(as.getOrderId());
            if (order != null) {
                as.setOrderNo(order.getOrderNo());
            }
        }
        return list;
    }

    @Override
    public void escalateAfterSale(Long userId, Long afterSaleId) {
        BizAfterSale afterSale = bizAfterSaleMapper.selectById(afterSaleId);
        if (afterSale == null || !Objects.equals(afterSale.getUserId(), userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "售后记录不存在");
        }
        if (!Integer.valueOf(2).equals(afterSale.getAuditStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅商家驳回的售后可申请平台介入");
        }
        if (Integer.valueOf(1).equals(afterSale.getEscalated())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已申请平台介入，请等待处理");
        }
        afterSale.setEscalated(1);
        afterSale.setAuditStatus(0);
        afterSale.setIsDeleted(afterSale.getIsDeleted() != null ? afterSale.getIsDeleted() : 0);
        bizAfterSaleMapper.updateById(afterSale);
    }

    @Override
    public void leaveMessage(Long userId, String userAccount, LeaveMessageRequest request) {
        BizLeaveMessage message = new BizLeaveMessage();
        message.setUserId(userId);
        message.setUserAccount(userAccount);
        message.setUserAvatar(getProfile(userId).getUserAvatar());
        message.setMessageTitle(request.getMessageTitle());
        message.setMessageContent(request.getMessageContent());
        message.setReplyContent("");
        bizLeaveMessageMapper.insert(message);
    }

    @Override
    public List<BizLeaveMessage> messageList(Long userId, String sort) {
        LambdaQueryWrapper<BizLeaveMessage> wrapper = new LambdaQueryWrapper<BizLeaveMessage>();
        if (userId != null) {
            wrapper.eq(BizLeaveMessage::getUserId, userId);
        }
        List<BizLeaveMessage> list = bizLeaveMessageMapper
                .selectList(wrapper.orderByDesc(BizLeaveMessage::getMessageId));
        for (BizLeaveMessage msg : list) {
            Long count = bizMessageCommentMapper.selectCount(new LambdaQueryWrapper<BizMessageComment>()
                    .eq(BizMessageComment::getMessageId, msg.getMessageId()));
            msg.setCommentCount(count.intValue());
        }
        if ("commentCount".equals(sort)) {
            list.sort((a, b) -> Integer.compare(
                    b.getCommentCount() != null ? b.getCommentCount() : 0,
                    a.getCommentCount() != null ? a.getCommentCount() : 0));
        }
        return list;
    }

    @Override
    public void deleteMessage(Long userId, Long messageId) {
        BizLeaveMessage message = bizLeaveMessageMapper.selectById(messageId);
        if (message == null || !Objects.equals(message.getUserId(), userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "留言不存在");
        }
        bizMessageCommentMapper.delete(new LambdaQueryWrapper<BizMessageComment>()
                .eq(BizMessageComment::getMessageId, messageId));
        bizLeaveMessageMapper.deleteById(messageId);
    }

    @Override
    public List<BizMessageComment> commentList(Long messageId) {
        return bizMessageCommentMapper.selectCommentsSorted(messageId);
    }

    @Override
    public void addComment(Long userId, String userAccount, String userAvatar, Long messageId, String commentContent) {
        BizMessageComment comment = new BizMessageComment();
        comment.setMessageId(messageId);
        comment.setUserId(userId);
        comment.setUserAccount(userAccount);
        comment.setUserAvatar(userAvatar);
        comment.setCommentContent(commentContent);
        bizMessageCommentMapper.insert(comment);
    }

    @Override
    public List<BizMessageComment> userCommentList(Long userId) {
        List<BizMessageComment> comments = bizMessageCommentMapper.selectList(new LambdaQueryWrapper<BizMessageComment>()
                .eq(BizMessageComment::getUserId, userId)
                .orderByDesc(BizMessageComment::getCommentId));
        for (BizMessageComment comment : comments) {
            if (comment.getMessageId() != null) {
                BizLeaveMessage message = bizLeaveMessageMapper.selectById(comment.getMessageId());
                if (message != null) {
                    comment.setMessageTitle(message.getMessageTitle());
                    comment.setMessageContent(message.getMessageContent());
                } else {
                    comment.setMessageTitle("留言已删除");
                    comment.setMessageContent("该留言已被删除");
                }
            } else {
                comment.setMessageTitle("无关联留言");
                comment.setMessageContent("该评论未关联任何留言");
            }
        }
        return comments;
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        BizMessageComment comment = bizMessageCommentMapper.selectById(commentId);
        if (comment == null || !Objects.equals(comment.getUserId(), userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "评论不存在");
        }
        bizMessageCommentMapper.deleteById(commentId);
    }

    @Override
    public void syncUserAvatar(Long userId) {
        BizUser user = bizUserMapper.selectById(userId);
        if (user == null || user.getUserAvatar() == null) {
            return;
        }
        bizLeaveMessageMapper.updateAvatarByUserId(userId, user.getUserAvatar());
        bizMessageCommentMapper.updateAvatarByUserId(userId, user.getUserAvatar());
    }

    private BizOrder getOwnedOrder(Long userId, Long orderId) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单不存在");
        }
        return order;
    }

    private void resetDefaultAddress(Long userId) {
        List<BizUserAddress> addresses = addressList(userId);
        for (BizUserAddress item : addresses) {
            if (item.getIsDefault() != null && item.getIsDefault() == 1) {
                item.setIsDefault(0);
                bizUserAddressMapper.updateById(item);
            }
        }
    }

    private void checkAddressOwner(Long userId, BizUserAddress address) {
        if (address == null || !Objects.equals(address.getUserId(), userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收货地址不存在");
        }
    }
}
