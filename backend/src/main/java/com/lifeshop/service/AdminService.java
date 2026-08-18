package com.lifeshop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeshop.dto.CategorySaveRequest;
import com.lifeshop.dto.MessageReplyRequest;
import com.lifeshop.entity.*;
import com.lifeshop.vo.AdminMerchantDetailVO;
import com.lifeshop.vo.AdminOrderDetailVO;
import com.lifeshop.vo.AdminUserDetailVO;
import com.lifeshop.vo.DashboardVO;

import java.util.List;

public interface AdminService {
  DashboardVO dashboard();

  Page<BizUser> userPage(long current, long size, String account, String phone);

  AdminUserDetailVO userDetail(Long userId);

  void toggleUserStatus(Long userId);

  Page<BizMerchant> merchantAuditPage(long current, long size, Integer auditStatus);

  void auditMerchant(Long merchantId, Integer auditStatus, String auditRemark);

  Page<BizMerchant> merchantPage(long current, long size, String merchantAccount, Integer merchantStatus);

  AdminMerchantDetailVO merchantDetail(Long merchantId);

  void toggleMerchantStatus(Long merchantId);

  void resetMerchantPassword(Long merchantId);

  Page<BizShop> shopPage(long current, long size, String shopName, Integer shopStatus);

  void toggleShopStatus(Long shopId);

  Page<BizCategory> categoryPage(long current, long size);

  List<BizCategory> categoryAll();

  void saveCategory(CategorySaveRequest request);

  void deleteCategory(Long categoryId);

  Page<BizGoods> goodsPage(long current, long size, String keyword, Long categoryId, Long shopId, Integer status);

  BizGoods goodsDetail(Long goodsId);

  void toggleGoodsStatus(Long goodsId);

  void deleteGoods(Long goodsId);

  Page<BizOrder> orderPage(long current, long size, String orderNo, Long userId, Long shopId, Integer status);

  AdminOrderDetailVO orderDetail(Long orderId);

  Page<BizLeaveMessage> messagePage(long current, long size, String userAccount, String title);

  void replyMessage(Long messageId, MessageReplyRequest request);

  void deleteMessage(Long messageId);

  Page<SysAdmin> adminPage(long current, long size, String adminAccount, Integer status);

  void addAdmin(SysAdmin admin);

  void toggleAdminStatus(Long adminId);

  void resetAdminPassword(Long adminId);

  void deleteAdmin(Long adminId);
}
