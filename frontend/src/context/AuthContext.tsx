import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { tokenStorage } from '@/lib/http';
import { authApi } from '@/services/api';
import type { AuthResponse, Role } from '@/types';

interface SessionUser {
  userId: string;
  name: string;
  role: Role;
}

interface AuthContextValue {
  user: SessionUser | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: { name: string; email: string; cpf: string; password: string }) => Promise<void>;
  logout: () => void;
}

const STORAGE_USER = 'itau_invest_user';
const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(null);

  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_USER);
    if (raw && tokenStorage.get()) {
      setUser(JSON.parse(raw) as SessionUser);
    }
  }, []);

  const persist = useCallback((auth: AuthResponse) => {
    const session: SessionUser = { userId: auth.userId, name: auth.name, role: auth.role };
    tokenStorage.set(auth.accessToken);
    localStorage.setItem(STORAGE_USER, JSON.stringify(session));
    setUser(session);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const auth = await authApi.login({ email, password });
      persist(auth);
    },
    [persist],
  );

  const register = useCallback(
    async (data: { name: string; email: string; cpf: string; password: string }) => {
      const auth = await authApi.register(data);
      persist(auth);
    },
    [persist],
  );

  const logout = useCallback(() => {
    tokenStorage.clear();
    localStorage.removeItem(STORAGE_USER);
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ user, isAuthenticated: Boolean(user), login, register, logout }),
    [user, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider');
  }
  return ctx;
}
