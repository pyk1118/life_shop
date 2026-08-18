const TOKEN_KEY = 'LIFE_SHOP_TOKEN';
const ROLE_KEY = 'LIFE_SHOP_ROLE';
const ACCOUNT_KEY = 'LIFE_SHOP_ACCOUNT';
const ID_KEY = 'LIFE_SHOP_ID';
const AVATAR_KEY = 'LIFE_SHOP_AVATAR';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function setLoginInfo(data) {
  setToken(data.token || '');
  localStorage.setItem(ROLE_KEY, data.role || '');
  localStorage.setItem(ACCOUNT_KEY, data.account || '');
  localStorage.setItem(ID_KEY, String(data.id || ''));
  localStorage.setItem(AVATAR_KEY, data.avatar || '');
}

export function getRole() {
  return localStorage.getItem(ROLE_KEY) || '';
}

export function getLoginInfo() {
  return {
    token: getToken(),
    role: getRole(),
    account: localStorage.getItem(ACCOUNT_KEY) || '',
    id: Number(localStorage.getItem(ID_KEY) || 0),
    avatar: localStorage.getItem(AVATAR_KEY) || ''
  };
}

export function clearLoginInfo() {
  removeToken();
  localStorage.removeItem(ROLE_KEY);
  localStorage.removeItem(ACCOUNT_KEY);
  localStorage.removeItem(ID_KEY);
  localStorage.removeItem(AVATAR_KEY);
}
