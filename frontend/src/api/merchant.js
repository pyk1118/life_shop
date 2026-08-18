import request from './index';

function silentConfig() {
  return { headers: { 'X-Silent': 'true' } };
}

export function getMerchantDashboard() {
  return request.get('/merchant/dashboard', silentConfig());
}

export function getMerchantShopInfo() {
  return request.get('/merchant/shop', silentConfig());
}

export function updateMerchantShop(data) {
  return request.put('/merchant/shop', data);
}

export function getMerchantGoods() {
  return request.get('/merchant/goods/list', silentConfig());
}

export function saveMerchantGoods(data) {
  return request.post('/merchant/goods/save', data);
}

export function toggleMerchantGoodsStatus(goodsId) {
  return request.post(`/merchant/goods/${goodsId}/toggle-status`);
}

export function getMerchantOrders() {
  return request.get('/merchant/order/list', silentConfig());
}

export function getMerchantOrderDetail(orderId) {
  return request.get(`/merchant/order/${orderId}`);
}

export function deliverMerchantOrder(orderId) {
  return request.post(`/merchant/order/${orderId}/deliver`);
}

export function getMerchantAfterSales() {
  return request.get('/merchant/after-sale/list', silentConfig());
}

export function handleMerchantAfterSale(afterSaleId, auditStatus, auditRemark) {
  return request.post(`/merchant/after-sale/${afterSaleId}/handle`, null, { params: { auditStatus, auditRemark: auditRemark || '' } });
}

export function changeMerchantPassword(data) {
  return request.post('/merchant/password/change', data);
}

export function getMerchantMessages() {
  return request.get('/merchant/message/list', silentConfig());
}

export function replyMerchantMessage(messageId, data) {
  return request.post(`/merchant/message/${messageId}/reply`, data);
}
