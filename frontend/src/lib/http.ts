import axios, { AxiosError } from 'axios';
import type { ApiErrorResponse } from '@/types';

const TOKEN_KEY = 'itau_invest_token';

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

// Anexa o token JWT em todas as requisicoes autenticadas.
http.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Em 401, limpa a sessao e redireciona para o login.
http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorResponse>) => {
    if (error.response?.status === 401 && tokenStorage.get()) {
      tokenStorage.clear();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  },
);

/** Extrai uma mensagem de erro amigavel da resposta da API. */
export function extractErrorMessage(error: unknown, fallback = 'Ocorreu um erro inesperado.'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiErrorResponse | undefined;
    if (data?.fieldErrors?.length) {
      return data.fieldErrors.map((f) => `${f.field}: ${f.message}`).join(' | ');
    }
    return data?.message ?? error.message ?? fallback;
  }
  return fallback;
}
