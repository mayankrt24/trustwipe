import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

// Add a request interceptor to include userEmail in all requests
api.interceptors.request.use(config => {
  const userEmail = localStorage.getItem('userEmail');
  if (userEmail) {
    config.params = {
      ...config.params,
      userEmail: userEmail
    };
  }
  return config;
}, error => {
  return Promise.reject(error);
});

export const authApi = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  signup: (email, password) => api.post('/auth/signup', { email, password }),
};

export const assetApi = {
  getAll: () => api.get('/assets'),
  scan: () => api.get('/drives/scan'),
  getById: (id) => api.get(`/assets/${id}`),
  listPath: (path) => api.get('/drives/list-path', { params: { path } }),
};

export const wipeApi = {
  fullWipe: (assetId, userEmail) => api.post(`/wipe/${assetId}`, null, { params: { userEmail } }),
  wipeFreeSpace: (assetId, userEmail) => api.post(`/wipe/free-space/${assetId}`, null, { params: { userEmail } }),
  partialWipe: (assetId, paths, userEmail) => api.post('/wipe/partial', { assetId, paths, userEmail }),
  getProgress: (assetId) => api.get(`/wipe/progress/${assetId}`),
};

export const reportApi = {
  getAll: () => api.get('/reports'),
};

export default api;
