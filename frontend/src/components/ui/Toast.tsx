import { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';
import { cn } from '@/lib/cn';

type ToastKind = 'success' | 'error' | 'info';
interface Toast {
  id: number;
  kind: ToastKind;
  message: string;
}

interface ToastContextValue {
  success: (message: string) => void;
  error: (message: string) => void;
  info: (message: string) => void;
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

const config: Record<ToastKind, { icon: ReactNode; accent: string }> = {
  success: { icon: <CheckCircle2 className="h-5 w-5 text-emerald-500" />, accent: 'border-l-emerald-500' },
  error: { icon: <AlertCircle className="h-5 w-5 text-rose-500" />, accent: 'border-l-rose-500' },
  info: { icon: <Info className="h-5 w-5 text-navy-500" />, accent: 'border-l-navy-500' },
};

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const counter = useRef(0);

  const remove = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const push = useCallback(
    (kind: ToastKind, message: string) => {
      const id = ++counter.current;
      setToasts((prev) => [...prev, { id, kind, message }]);
      window.setTimeout(() => remove(id), 4500);
    },
    [remove],
  );

  const value = useMemo<ToastContextValue>(
    () => ({
      success: (m) => push('success', m),
      error: (m) => push('error', m),
      info: (m) => push('info', m),
    }),
    [push],
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      {createPortal(
        <div className="pointer-events-none fixed right-4 top-4 z-[60] flex w-full max-w-sm flex-col gap-2.5">
          {toasts.map((toast) => (
            <div
              key={toast.id}
              className={cn(
                'pointer-events-auto flex items-start gap-3 rounded-xl border border-slate-200 border-l-4 bg-white p-3.5 shadow-card-hover animate-slide-in-right',
                config[toast.kind].accent,
              )}
            >
              {config[toast.kind].icon}
              <p className="flex-1 text-sm font-medium text-slate-700">{toast.message}</p>
              <button
                onClick={() => remove(toast.id)}
                className="text-slate-400 transition-colors hover:text-slate-600"
                aria-label="Fechar"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          ))}
        </div>,
        document.body,
      )}
    </ToastContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast deve ser usado dentro de ToastProvider');
  return ctx;
}
