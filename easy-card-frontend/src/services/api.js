import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authService = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

export const cardService = {
  applyForCard: (data) => api.post('/cards/apply', data),
  getMyCards: () => api.get('/cards'),
  getCardById: (id) => api.get(`/cards/${id}`),
  getAllCards: () => api.get('/cards/admin/all'),
  getPendingCards: () => api.get('/cards/admin/pending'),
  activateCard: (id) => api.put(`/cards/${id}/activate`),
  blockCard: (id) => api.put(`/cards/${id}/block`),
  updateLimit: (id, limit) => api.put(`/cards/${id}/limit?limit=${limit}`),
};

export const transactionService = {
  processTransaction: (data) => api.post('/transactions', data),
  getCardTransactions: (cardId) => api.get(`/transactions/card/${cardId}`),
  getMyTransactions: () => api.get('/transactions'),
};

export default api;
