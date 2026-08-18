import request from './index';

export function pingHome() {
  return request.get('/public/ping');
}

export function login(data) {
  return request.post('/public/auth/login', data);
}

export function register(data) {
  return request.post('/public/auth/register', data);
}

export function getCategoryList() {
  return request.get('/public/categories');
}

export function getGoodsPage(params) {
  return request.get('/public/goods', { params });
}

export function getGoodsDetail(goodsId) {
  return request.get(`/public/goods/${goodsId}`);
}

export function getShopList() {
  return request.get('/public/shops');
}

export function getPublicMessageList(params) {
  return request.get('/public/messages', { params });
}

export function getMessageComments(messageId) {
  return request.get(`/public/message/comments/${messageId}`);
}
