import axios from 'axios';
import { ElMessage } from 'element-plus';
import { getToken, removeToken } from '../utils/auth';

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
});

request.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

function isSilent(config) {
  return config && (config._silent === true || config.headers?.['X-Silent'] === 'true');
}

request.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      if (!isSilent(response.config)) {
        ElMessage.error(res.message || '请求失败');
      }
      return Promise.reject(res);
    }
    return res;
  },
  (error) => {
    if (!isSilent(error.config)) {
      const status = error?.response?.status;
      if (status === 401) {
        removeToken();
        ElMessage.error('登录已过期，请重新登录');
        window.location.href = '/login';
      } else if (status === 403) {
        ElMessage.error('无权限访问该资源');
      } else {
        ElMessage.error(error?.response?.data?.message || '网络异常，请稍后重试');
      }
    }
    return Promise.reject(error);
  }
);

export default request;
