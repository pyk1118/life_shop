import request from './index';

export function getDashboard() {
  return request.get('/admin/dashboard');
}

export function getUserList(params) {
  return request.get('/admin/user/list', { params });
}

export function getUserDetail(userId) {
  return request.get(`/admin/user/${userId}`);
}

export function toggleUserStatus(userId) {
  return request.post(`/admin/user/${userId}/toggle-status`);
}

export function getMerchantAuditList(params) {
  return request.get('/admin/merchant/audit/list', { params });
}

export function auditMerchant(merchantId, auditStatus, auditRemark) {
  return request.post(`/admin/merchant/${merchantId}/audit`, null, {
    params: { auditStatus, auditRemark }
  });
}

export function getMerchantList(params) {
  return request.get('/admin/merchant/list', { params });
}

export function getMerchantDetail(merchantId) {
  return request.get(`/admin/merchant/${merchantId}`);
}

export function toggleMerchantStatus(merchantId) {
  return request.post(`/admin/merchant/${merchantId}/toggle-status`);
}

export function resetMerchantPassword(merchantId) {
  return request.post(`/admin/merchant/${merchantId}/reset-password`);
}

export function getShopList(params) {
  return request.get('/admin/shop/list', { params });
}

export function toggleShopStatus(shopId) {
  return request.post(`/admin/shop/${shopId}/toggle-status`);
}

export function getCategoryList(params) {
  return request.get('/admin/category/list', { params });
}

export function getCategoryAll() {
  return request.get('/admin/category/all');
}

export function saveCategory(data) {
  return request.post('/admin/category/save', data);
}

export function deleteCategory(categoryId) {
  return request.delete(`/admin/category/${categoryId}`);
}

export function getGoodsList(params) {
  return request.get('/admin/goods/list', { params });
}

export function getGoodsDetail(goodsId) {
  return request.get(`/admin/goods/${goodsId}`);
}

export function toggleGoodsStatus(goodsId) {
  return request.post(`/admin/goods/${goodsId}/toggle-status`);
}

export function deleteGoods(goodsId) {
  return request.delete(`/admin/goods/${goodsId}`);
}

export function getOrderList(params) {
  return request.get('/admin/order/list', { params });
}

export function getOrderDetail(orderId) {
  return request.get(`/admin/order/${orderId}`);
}

export function getMessageList(params) {
  return request.get('/admin/message/list', { params });
}

export function replyMessage(messageId, data) {
  return request.post(`/admin/message/${messageId}/reply`, data);
}

export function deleteMessage(messageId) {
  return request.delete(`/admin/message/${messageId}`);
}

export function changeAdminPassword(data) {
  return request.post('/admin/password/change', data);
}

export function getAdminList(params) {
  return request.get('/admin/admin/list', { params });
}

export function addAdmin(data) {
  return request.post('/admin/admin/add', data);
}

export function toggleAdminStatus(adminId) {
  return request.post(`/admin/admin/${adminId}/toggle-status`);
}

export function resetAdminPassword(adminId) {
  return request.post(`/admin/admin/${adminId}/reset-password`);
}

export function deleteAdmin(adminId) {
  return request.delete(`/admin/admin/${adminId}`);
}

export function getAdminAfterSaleList() {
  return request.get('/admin/after-sale/list');
}

export function handleAdminAfterSale(afterSaleId, auditStatus, adminRemark) {
  return request.post(`/admin/after-sale/${afterSaleId}/handle`, null, {
    params: { auditStatus, adminRemark: adminRemark || '' }
  });
}
