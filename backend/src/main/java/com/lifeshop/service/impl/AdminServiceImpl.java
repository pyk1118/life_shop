package com.lifeshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeshop.common.enums.OrderStatus;
import com.lifeshop.common.exception.BusinessException;
import com.lifeshop.common.result.ResultCode;
import com.lifeshop.dto.CategorySaveRequest;
import com.lifeshop.dto.MessageReplyRequest;
import com.lifeshop.entity.*;
import com.lifeshop.mapper.*;
import com.lifeshop.service.AdminService;
import com.lifeshop.vo.AdminMerchantDetailVO;
import com.lifeshop.vo.AdminOrderDetailVO;
import com.lifeshop.vo.AdminUserDetailVO;
import com.lifeshop.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final BizUserMapper bizUserMapper;
  private final BizMerchantMapper bizMerchantMapper;
  private final BizShopMapper bizShopMapper;
  private final BizCategoryMapper bizCategoryMapper;
  private final BizGoodsMapper bizGoodsMapper;
  private final BizOrderMapper bizOrderMapper;
  private final BizOrderItemMapper bizOrderItemMapper;
  private final BizLeaveMessageMapper bizLeaveMessageMapper;
  private final BizUserAddressMapper bizUserAddressMapper;
  private final SysAdminMapper sysAdminMapper;
  private final BizMessageCommentMapper bizMessageCommentMapper;
  private final BizAfterSaleMapper bizAfterSaleMapper;
  private final com.lifeshop.service.EmailService emailService;

  @Override
  public DashboardVO dashboard() {
    DashboardVO vo = new DashboardVO();
    vo.setUserCount(bizUserMapper.selectCount(new LambdaQueryWrapper<>()));
    vo.setMerchantCount(bizMerchantMapper.selectCount(new LambdaQueryWrapper<BizMerchant>()
        .eq(BizMerchant::getAuditStatus, 1)));
    vo.setShopCount(bizShopMapper.selectCount(new LambdaQueryWrapper<BizShop>()
        .eq(BizShop::getShopStatus, 1)));
    vo.setGoodsCount(bizGoodsMapper.selectCount(new LambdaQueryWrapper<BizGoods>()
        .eq(BizGoods::getStatus, 1)));
    vo.setOrderCount(bizOrderMapper.countAllForAdmin());
    vo.setMessageCount(bizLeaveMessageMapper.selectCount(new LambdaQueryWrapper<>()));

    List<BizOrder> paidOrders = bizOrderMapper.selectPaidOrdersForAdmin();
    BigDecimal totalSales = paidOrders.stream()
        .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    vo.setTotalSales(totalSales);

    vo.setPendingAuditCount(bizMerchantMapper.selectCount(new LambdaQueryWrapper<BizMerchant>()
        .eq(BizMerchant::getAuditStatus, 0)));
    vo.setAbnormalOrderCount(bizAfterSaleMapper.selectCount(new LambdaQueryWrapper<BizAfterSale>()
        .eq(BizAfterSale::getEscalated, 1)
        .eq(BizAfterSale::getAuditStatus, 0)));
    vo.setUnrepliedMessageCount(bizLeaveMessageMapper.countUnrepliedForAdmin());

    List<Map<String, Object>> monthlySales = new ArrayList<>();
    LocalDate today = LocalDate.now();
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    java.util.Map<String, BigDecimal> monthlySalesMap = new java.util.TreeMap<>();
    java.util.Map<String, Integer> monthlyCountMap = new java.util.TreeMap<>();
    for (int i = 5; i >= 0; i--) {
      LocalDate monthDate = today.minusMonths(i);
      String monthStr = monthDate.format(monthFormatter);
      monthlySalesMap.put(monthStr, BigDecimal.ZERO);
      monthlyCountMap.put(monthStr, 0);
    }
    DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM-dd");
    java.util.Map<String, BigDecimal> dailySalesMap = new java.util.TreeMap<>();
    java.util.Map<String, Integer> dailyCountMap = new java.util.TreeMap<>();
    for (int i = 29; i >= 0; i--) {
      LocalDate dayDate = today.minusDays(i);
      String dayStr = dayDate.format(dayFormatter);
      dailySalesMap.put(dayStr, BigDecimal.ZERO);
      dailyCountMap.put(dayStr, 0);
    }
    for (BizOrder o : paidOrders) {
      if (o.getPayTime() != null && o.getTotalAmount() != null) {
        String month = o.getPayTime().format(monthFormatter);
        String day = o.getPayTime().format(dayFormatter);
        if (monthlySalesMap.containsKey(month)) {
          monthlySalesMap.merge(month, o.getTotalAmount(), BigDecimal::add);
          monthlyCountMap.merge(month, 1, Integer::sum);
        }
        if (dailySalesMap.containsKey(day)) {
          dailySalesMap.merge(day, o.getTotalAmount(), BigDecimal::add);
          dailyCountMap.merge(day, 1, Integer::sum);
        }
      }
    }
    for (var entry : monthlySalesMap.entrySet()) {
      Map<String, Object> item = new HashMap<>();
      item.put("month", entry.getKey());
      item.put("sales", entry.getValue());
      item.put("orderCount", monthlyCountMap.getOrDefault(entry.getKey(), 0));
      monthlySales.add(item);
    }
    vo.setMonthlySales(monthlySales);

    List<Map<String, Object>> dailySales = new ArrayList<>();
    for (var entry : dailySalesMap.entrySet()) {
      Map<String, Object> item = new HashMap<>();
      item.put("date", entry.getKey());
      item.put("sales", entry.getValue());
      item.put("orderCount", dailyCountMap.getOrDefault(entry.getKey(), 0));
      dailySales.add(item);
    }
    vo.setDailySales(dailySales);

    return vo;
  }

  @Override
  public Page<BizUser> userPage(long current, long size, String account, String phone) {
    LambdaQueryWrapper<BizUser> qw = new LambdaQueryWrapper<BizUser>()
        .like(account != null && !account.isBlank(), BizUser::getUserAccount, account)
        .like(phone != null && !phone.isBlank(), BizUser::getPhone, phone)
        .orderByAsc(BizUser::getUserId);
    Page<BizUser> page = bizUserMapper.selectPage(new Page<>(current, size), qw);
    page.getRecords().forEach(u -> u.setUserPassword(null));
    return page;
  }

  @Override
  public AdminUserDetailVO userDetail(Long userId) {
    BizUser user = bizUserMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在");
    }
    user.setUserPassword(null);

    List<BizOrder> orders = bizOrderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
        .eq(BizOrder::getUserId, userId)
        .orderByAsc(BizOrder::getOrderId));
    List<BizUserAddress> addresses = bizUserAddressMapper.selectList(new LambdaQueryWrapper<BizUserAddress>()
        .eq(BizUserAddress::getUserId, userId));

    AdminUserDetailVO vo = new AdminUserDetailVO();
    vo.setUserInfo(user);
    vo.setOrderList(orders.stream().map(this::orderToMap).collect(Collectors.toList()));
    vo.setAddressList(addresses.stream().map(this::addressToMap).collect(Collectors.toList()));
    return vo;
  }

  @Override
  public void toggleUserStatus(Long userId) {
    BizUser user = bizUserMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在");
    }
    user.setStatus(user.getStatus() == 1 ? 0 : 1);
    bizUserMapper.updateById(user);
  }

  @Override
  public Page<BizMerchant> merchantAuditPage(long current, long size, Integer auditStatus) {
    LambdaQueryWrapper<BizMerchant> qw = new LambdaQueryWrapper<BizMerchant>()
        .eq(auditStatus != null, BizMerchant::getAuditStatus, auditStatus)
        .orderByAsc(BizMerchant::getMerchantId);
    Page<BizMerchant> page = bizMerchantMapper.selectPage(new Page<>(current, size), qw);
    page.getRecords().forEach(m -> m.setMerchantPassword(null));
    return page;
  }

  @Override
  @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
  public void auditMerchant(Long merchantId, Integer auditStatus, String auditRemark) {
    BizMerchant merchant = bizMerchantMapper.selectById(merchantId);
    if (merchant == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "商家申请不存在");
    }
    merchant.setAuditStatus(auditStatus);
    merchant.setAuditRemark(auditRemark);
    bizMerchantMapper.updateById(merchant);

    BizUser user = bizUserMapper.selectById(merchant.getUserId());
    if (user != null) {
      user.setIsMerchant(auditStatus == 1 ? 1 : 0);
      bizUserMapper.updateById(user);
    }
    if (merchant.getShopId() != null) {
            BizShop shop = bizShopMapper.selectById(merchant.getShopId());
            if (shop != null) {
                shop.setShopStatus(auditStatus == 1 ? 1 : 0);
                bizShopMapper.updateById(shop);
                if (user != null && user.getEmail() != null) {
                    emailService.sendMerchantAuditResult(user.getEmail(), shop.getShopName(), merchant.getMerchantAccount(), auditStatus == 1, auditRemark);
                }
            }
        }
  }

  @Override
  public Page<BizMerchant> merchantPage(long current, long size, String merchantAccount, Integer merchantStatus) {
    LambdaQueryWrapper<BizMerchant> qw = new LambdaQueryWrapper<BizMerchant>()
        .eq(BizMerchant::getAuditStatus, 1)
        .like(merchantAccount != null && !merchantAccount.isBlank(), BizMerchant::getMerchantAccount, merchantAccount)
        .eq(merchantStatus != null, BizMerchant::getMerchantStatus, merchantStatus)
        .orderByAsc(BizMerchant::getMerchantId);
    Page<BizMerchant> page = bizMerchantMapper.selectPage(new Page<>(current, size), qw);
    page.getRecords().forEach(m -> m.setMerchantPassword(null));
    return page;
  }

  @Override
  public AdminMerchantDetailVO merchantDetail(Long merchantId) {
    BizMerchant merchant = bizMerchantMapper.selectById(merchantId);
    if (merchant == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "商家不存在");
    }
    merchant.setMerchantPassword(null);

    BizUser user = bizUserMapper.selectById(merchant.getUserId());
    if (user != null)
      user.setUserPassword(null);

    BizShop shop = null;
    if (merchant.getShopId() != null) {
      shop = bizShopMapper.selectById(merchant.getShopId());
    }

    AdminMerchantDetailVO vo = new AdminMerchantDetailVO();
    vo.setMerchantInfo(merchant);
    vo.setUserInfo(user);
    vo.setShopInfo(shop);
    return vo;
  }

  @Override
  public void toggleMerchantStatus(Long merchantId) {
    BizMerchant merchant = bizMerchantMapper.selectById(merchantId);
    if (merchant == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "商家不存在");
    }
    merchant.setMerchantStatus(merchant.getMerchantStatus() == 1 ? 0 : 1);
    bizMerchantMapper.updateById(merchant);
  }

  @Override
  public void resetMerchantPassword(Long merchantId) {
    BizMerchant merchant = bizMerchantMapper.selectById(merchantId);
    if (merchant == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "商家不存在");
    }
    merchant.setMerchantPassword("123456");
    bizMerchantMapper.updateById(merchant);
  }

  @Override
  public Page<BizShop> shopPage(long current, long size, String shopName, Integer shopStatus) {
    long offset = (current - 1) * size;
    List<BizShop> records = bizShopMapper.selectApprovedShopPage(shopName, shopStatus);
    Long total = bizShopMapper.countApprovedShopPage(shopName, shopStatus);
    Page<BizShop> page = new Page<>(current, size);
    page.setRecords(records.stream().skip(offset).limit(size).collect(Collectors.toList()));
    page.setTotal(total);
    return page;
  }

  @Override
  public void toggleShopStatus(Long shopId) {
    BizShop shop = bizShopMapper.selectById(shopId);
    if (shop == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "店铺不存在");
    }
    shop.setShopStatus(shop.getShopStatus() == 1 ? 0 : 1);
    bizShopMapper.updateById(shop);
  }

  @Override
  public Page<BizCategory> categoryPage(long current, long size) {
    LambdaQueryWrapper<BizCategory> qw = new LambdaQueryWrapper<BizCategory>()
        .orderByAsc(BizCategory::getCategoryId);
    return bizCategoryMapper.selectPage(new Page<>(current, size), qw);
  }

  @Override
  public List<BizCategory> categoryAll() {
    return bizCategoryMapper.selectList(new LambdaQueryWrapper<BizCategory>()
        .orderByAsc(BizCategory::getSort));
  }

  @Override
  public void saveCategory(CategorySaveRequest request) {
    if (request.getCategoryId() == null) {
      BizCategory category = new BizCategory();
      category.setCategoryName(request.getCategoryName());
      category.setCategoryDesc(request.getCategoryDesc() == null ? "" : request.getCategoryDesc());
      category.setCategoryIcon(request.getCategoryIcon() == null ? "" : request.getCategoryIcon());
      category.setSort(request.getSort() == null ? 0 : request.getSort());
      category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
      bizCategoryMapper.insert(category);
    } else {
      BizCategory category = bizCategoryMapper.selectById(request.getCategoryId());
      if (category == null) {
        throw new BusinessException(ResultCode.BAD_REQUEST, "分类不存在");
      }
      if (request.getCategoryName() != null)
        category.setCategoryName(request.getCategoryName());
      if (request.getCategoryDesc() != null)
        category.setCategoryDesc(request.getCategoryDesc());
      if (request.getCategoryIcon() != null)
        category.setCategoryIcon(request.getCategoryIcon());
      if (request.getSort() != null)
        category.setSort(request.getSort());
      if (request.getStatus() != null)
        category.setStatus(request.getStatus());
      bizCategoryMapper.updateById(category);
    }
  }

  @Override
  public void deleteCategory(Long categoryId) {
    Long goodsCount = bizGoodsMapper.selectCount(new LambdaQueryWrapper<BizGoods>()
        .eq(BizGoods::getCategoryId, categoryId));
    if (goodsCount > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "该分类下存在商品，禁止删除");
    }
    bizCategoryMapper.deleteById(categoryId);
  }

  @Override
  public Page<BizGoods> goodsPage(long current, long size, String keyword, Long categoryId, Long shopId,
      Integer status) {
    LambdaQueryWrapper<BizGoods> qw = new LambdaQueryWrapper<BizGoods>()
        .like(keyword != null && !keyword.isBlank(), BizGoods::getGoodsName, keyword)
        .eq(categoryId != null, BizGoods::getCategoryId, categoryId)
        .eq(shopId != null, BizGoods::getShopId, shopId)
        .eq(status != null, BizGoods::getStatus, status)
        .orderByAsc(BizGoods::getGoodsId);
    return bizGoodsMapper.selectPage(new Page<>(current, size), qw);
  }

  @Override
  public BizGoods goodsDetail(Long goodsId) {
    BizGoods goods = bizGoodsMapper.selectById(goodsId);
    if (goods == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在");
    }
    return goods;
  }

  @Override
  public void toggleGoodsStatus(Long goodsId) {
    BizGoods goods = bizGoodsMapper.selectById(goodsId);
    if (goods == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在");
    }
    goods.setStatus(goods.getStatus() == 1 ? 0 : 1);
    bizGoodsMapper.updateById(goods);
  }

  @Override
  public void deleteGoods(Long goodsId) {
    bizGoodsMapper.deleteById(goodsId);
  }

  @Override
  public Page<BizOrder> orderPage(long current, long size, String orderNo, Long userId, Long shopId, Integer status) {
    long offset = (current - 1) * size;
    List<BizOrder> records = bizOrderMapper.selectAllForAdminWithFilter(offset, size, orderNo, userId, shopId, status);
    Long total = bizOrderMapper.countAllForAdminWithFilter(orderNo, userId, shopId, status);
    
    Page<BizOrder> page = new Page<>(current, size);
    page.setRecords(records);
    page.setTotal(total);
    return page;
  }

  @Override
  public AdminOrderDetailVO orderDetail(Long orderId) {
    BizOrder order = bizOrderMapper.selectByIdIgnoreDelete(orderId);
    if (order == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "订单不存在");
    }
    List<BizOrderItem> items = bizOrderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
        .eq(BizOrderItem::getOrderId, orderId));

    BizUser user = bizUserMapper.selectById(order.getUserId());
    if (user != null)
      user.setUserPassword(null);

    BizShop shop = bizShopMapper.selectById(order.getShopId());

    AdminOrderDetailVO vo = new AdminOrderDetailVO();
    vo.setOrderInfo(order);
    vo.setOrderItems(items.stream().map(this::itemToMap).collect(Collectors.toList()));
    vo.setUserInfo(user);
    vo.setShopInfo(shop);
    return vo;
  }

  @Override
  public Page<BizLeaveMessage> messagePage(long current, long size, String userAccount, String title) {
    LambdaQueryWrapper<BizLeaveMessage> qw = new LambdaQueryWrapper<BizLeaveMessage>()
        .like(userAccount != null && !userAccount.isBlank(), BizLeaveMessage::getUserAccount, userAccount)
        .like(title != null && !title.isBlank(), BizLeaveMessage::getMessageTitle, title)
        .orderByAsc(BizLeaveMessage::getMessageId);
    return bizLeaveMessageMapper.selectPage(new Page<>(current, size), qw);
  }

  @Override
  public void replyMessage(Long messageId, MessageReplyRequest request) {
    BizLeaveMessage message = bizLeaveMessageMapper.selectById(messageId);
    if (message == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "留言不存在");
    }
    message.setReplyContent(request.getReplyContent());
    bizLeaveMessageMapper.updateById(message);
    BizMessageComment existingComment = bizMessageCommentMapper.selectOne(new LambdaQueryWrapper<BizMessageComment>()
        .eq(BizMessageComment::getMessageId, messageId)
        .eq(BizMessageComment::getUserId, 0L)
        .eq(BizMessageComment::getIsTop, 1));
    if (existingComment != null) {
      existingComment.setCommentContent(request.getReplyContent());
      bizMessageCommentMapper.updateById(existingComment);
    } else {
      BizMessageComment comment = new BizMessageComment();
      comment.setMessageId(messageId);
      comment.setUserId(0L);
      comment.setUserAccount("开发者回复");
      comment.setUserAvatar("");
      comment.setCommentContent(request.getReplyContent());
      comment.setIsTop(1);
      bizMessageCommentMapper.insert(comment);
    }
    if (message.getMessageTitle() != null && message.getMessageTitle().contains("@开发者") && message.getUserId() != null) {
      BizUser msgUser = bizUserMapper.selectById(message.getUserId());
      if (msgUser != null && msgUser.getEmail() != null) {
        emailService.sendMessageReplyNotification(msgUser.getEmail(), message.getMessageTitle(), message.getMessageContent(), request.getReplyContent());
      }
    }
  }

  @Override
  public void deleteMessage(Long messageId) {
    bizMessageCommentMapper.delete(new LambdaQueryWrapper<BizMessageComment>()
        .eq(BizMessageComment::getMessageId, messageId));
    bizLeaveMessageMapper.deleteById(messageId);
  }

  private Map<String, Object> orderToMap(BizOrder order) {
    Map<String, Object> map = new HashMap<>();
    map.put("orderId", order.getOrderId());
    map.put("orderNo", order.getOrderNo());
    map.put("totalAmount", order.getTotalAmount());
    map.put("orderStatus", order.getOrderStatus());
    return map;
  }

  private Map<String, Object> addressToMap(BizUserAddress addr) {
    Map<String, Object> map = new HashMap<>();
    map.put("addressId", addr.getAddressId());
    map.put("receiverName", addr.getReceiverName());
    map.put("receiverPhone", addr.getReceiverPhone());
    map.put("receiverAddress", addr.getReceiverAddress());
    map.put("isDefault", addr.getIsDefault());
    return map;
  }

  private Map<String, Object> itemToMap(BizOrderItem item) {
    Map<String, Object> map = new HashMap<>();
    map.put("itemId", item.getItemId());
    map.put("goodsId", item.getGoodsId());
    map.put("goodsName", item.getGoodsName());
    map.put("goodsImage", item.getGoodsImage());
    map.put("goodsPrice", item.getGoodsPrice());
    map.put("goodsNum", item.getGoodsNum());
    map.put("totalPrice", item.getTotalPrice());
    return map;
  }

  @Override
  public Page<SysAdmin> adminPage(long current, long size, String adminAccount, Integer status) {
    LambdaQueryWrapper<SysAdmin> qw = new LambdaQueryWrapper<SysAdmin>()
        .like(adminAccount != null && !adminAccount.isBlank(), SysAdmin::getAdminAccount, adminAccount)
        .eq(status != null, SysAdmin::getStatus, status)
        .orderByAsc(SysAdmin::getAdminId);
    Page<SysAdmin> page = sysAdminMapper.selectPage(new Page<>(current, size), qw);
    page.getRecords().forEach(a -> a.setAdminPassword(null));
    return page;
  }

  @Override
  public void addAdmin(SysAdmin admin) {
    Long count = sysAdminMapper.selectCount(new LambdaQueryWrapper<SysAdmin>()
        .eq(SysAdmin::getAdminAccount, admin.getAdminAccount()));
    if (count > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "该管理员账号已存在");
    }
    admin.setAdminPassword(admin.getAdminPassword() == null ? "123456" : admin.getAdminPassword());
    admin.setStatus(1);
    sysAdminMapper.insert(admin);
  }

  @Override
  public void toggleAdminStatus(Long adminId) {
    SysAdmin admin = sysAdminMapper.selectById(adminId);
    if (admin == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "管理员不存在");
    }
    if ("admin".equals(admin.getAdminAccount())) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "超级管理员不可禁用");
    }
    admin.setStatus(admin.getStatus() == 1 ? 0 : 1);
    sysAdminMapper.updateById(admin);
  }

  @Override
  public void resetAdminPassword(Long adminId) {
    SysAdmin admin = sysAdminMapper.selectById(adminId);
    if (admin == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "管理员不存在");
    }
    admin.setAdminPassword("123456");
    sysAdminMapper.updateById(admin);
  }

  @Override
  public void deleteAdmin(Long adminId) {
    SysAdmin admin = sysAdminMapper.selectById(adminId);
    if (admin == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "管理员不存在");
    }
    if ("admin".equals(admin.getAdminAccount())) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "超级管理员不可删除");
    }
    sysAdminMapper.deleteById(adminId);
  }
}
