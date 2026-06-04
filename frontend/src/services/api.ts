import { http } from '@/lib/http';
import type {
  AuthResponse,
  Page,
  Portfolio,
  Product,
  Transaction,
  Wallet,
} from '@/types';

export const authApi = {
  register: (payload: { name: string; email: string; cpf: string; password: string }) =>
    http.post<AuthResponse>('/auth/register', payload).then((r) => r.data),
  login: (payload: { email: string; password: string }) =>
    http.post<AuthResponse>('/auth/login', payload).then((r) => r.data),
};

export const productApi = {
  list: (type?: string) =>
    http
      .get<Page<Product>>('/products', { params: { type, size: 50 } })
      .then((r) => r.data),
};

export const walletApi = {
  get: () => http.get<Wallet>('/wallet').then((r) => r.data),
  deposit: (amount: number, description?: string) =>
    http.post<Wallet>('/wallet/deposits', { amount, description }).then((r) => r.data),
  withdraw: (amount: number, description?: string) =>
    http.post<Wallet>('/wallet/withdrawals', { amount, description }).then((r) => r.data),
  statement: (page = 0) =>
    http
      .get<Page<Transaction>>('/wallet/statement', { params: { page, size: 20 } })
      .then((r) => r.data),
};

export const investmentApi = {
  portfolio: () => http.get<Portfolio>('/investments/portfolio').then((r) => r.data),
  apply: (productId: string, amount: number) =>
    http.post('/investments/applications', { productId, amount }).then((r) => r.data),
  redeem: (investmentId: string, amount: number) =>
    http
      .post<Portfolio>(`/investments/${investmentId}/redemptions`, { amount })
      .then((r) => r.data),
};
