import request from './index';

export function getProfile() {
  return request.get('/user/profile');
}

export function updateProfile(data) {
  return request.put('/user/profile', data);
}

export function uploadAvatar(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function changeUserPassword(data) {
  return request.post('/user/password/change', data);
}

export function getAddressList() {
  return request.get('/user/address/list');
}

export function saveAddress(data) {
  return request.post('/user/address/save', data);
}

export function deleteAddress(addressId) {
  return request.delete(`/user/address/${addressId}`);
}

export function getCartList() {
  return request.get('/user/cart/list');
}

export function addCart(data) {
  return request.post('/user/cart/add', data);
}

export function updateCartNum(data) {
  return request.put('/user/cart/update', data);
}

export function deleteCartItem(cartId) {
  return request.delete(`/user/cart/${cartId}`);
}

export function createOrder(data) {
  return request.post('/user/order/submit', data);
}

export function getOrderList() {
  return request.get('/user/order/list', {
    params: {
      current: 1,
      size: 100
    }
  });
}

export function payOrderApi(orderId) {
  return request.post(`/user/order/${orderId}/pay`);
}

export function cancelOrderApi(orderId) {
  return request.post(`/user/order/${orderId}/cancel`);
}

export function confirmReceiveOrder(orderId) {
  return request.post(`/user/order/${orderId}/confirm-receive`);
}

export function applyRefundOrder(orderId) {
  return request.post(`/user/order/${orderId}/refund`);
}

export function deleteOrderApi(orderId) {
  return request.delete(`/user/order/${orderId}`);
}

export function getOrderDetail(orderId) {
  return request.get(`/user/order/${orderId}`);
}

export function applyMerchant(data) {
  return request.post('/user/merchant/apply', data);
}

export function getMerchantProgress() {
  return request.get('/user/merchant/progress');
}

export function getAfterSaleList() {
  return request.get('/user/after-sale/list');
}

export function applyAfterSale(data) {
  return request.post('/user/after-sale/apply', data);
}

export function escalateAfterSale(afterSaleId) {
  return request.post(`/user/after-sale/${afterSaleId}/escalate`);
}

export function createMessage(data) {
  return request.post('/user/message', data);
}

export function createComment(data) {
  return request.post('/user/message/comment', data);
}

export function getMessageList() {
  return request.get('/user/message/list');
}

export function deleteMessage(messageId) {
  return request.delete(`/user/message/${messageId}`);
}

export function updateMessage(data) {
  return request.put('/user/message', data);
}

export function getCommentList() {
  return request.get('/user/comment/list');
}

export function deleteComment(commentId) {
  return request.delete(`/user/comment/${commentId}`);
}

export function getMessageComments(messageId) {
  return request.get(`/user/message/${messageId}/comments`);
}
