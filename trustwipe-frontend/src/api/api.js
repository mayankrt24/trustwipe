import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
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
  fullWipe: (assetId) => api.post(`/wipe/${assetId}`),
  partialWipe: (assetId, paths) => api.post('/wipe/partial', { assetId, paths }),
  getProgress: (assetId) => api.get(`/wipe/progress/${assetId}`),
};

export const reportApi = {
  getAll: () => api.get('/reports'),
};

export default api;
