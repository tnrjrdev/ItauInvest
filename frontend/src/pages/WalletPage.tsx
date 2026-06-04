import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  ArrowDownLeft,
  ArrowDownToLine,
  ArrowUpFromLine,
  ArrowUpRight,
  Receipt,
} from 'lucide-react';
import { walletApi } from '@/services/api';
import { extractErrorMessage } from '@/lib/http';
import { formatCurrency, formatDate, transactionLabel } from '@/lib/format';
import { Button } from '@/components/ui/Button';
import { Card, CardHeader } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { EmptyState } from '@/components/ui/feedback';
import { useToast } from '@/components/ui/Toast';
import { cn } from '@/lib/cn';
import type { Transaction, TransactionType, Wallet } from '@/types';

type Mode = 'deposit' | 'withdraw';

const txIcon: Record<TransactionType, JSX.Element> = {
  DEPOSIT: <ArrowDownLeft className="h-4 w-4 text-emerald-600" />,
  REDEMPTION: <ArrowDownLeft className="h-4 w-4 text-emerald-600" />,
  WITHDRAWAL: <ArrowUpRight className="h-4 w-4 text-rose-600" />,
  APPLICATION: <ArrowUpRight className="h-4 w-4 text-rose-600" />,
};

export default function WalletPage() {
  const toast = useToast();
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [mode, setMode] = useState<Mode>('deposit');
  const [amount, setAmount] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [w, statement] = await Promise.all([walletApi.get(), walletApi.statement()]);
      setWallet(w);
      setTransactions(statement.content);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => {
    void load();
  }, [load]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const value = Number(amount);
    if (!value || value <= 0) {
      toast.error('Informe um valor maior que zero.');
      return;
    }
    setSubmitting(true);
    try {
      const updated = mode === 'deposit' ? await walletApi.deposit(value) : await walletApi.withdraw(value);
      setWallet(updated);
      toast.success(`${mode === 'deposit' ? 'Deposito' : 'Saque'} de ${formatCurrency(value)} realizado.`);
      setAmount('');
      const statement = await walletApi.statement();
      setTransactions(statement.content);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Caixa</h1>
        <p className="text-sm text-slate-500">Deposite, saque e acompanhe seu extrato.</p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Saldo + operacoes */}
        <div className="space-y-6 lg:col-span-1">
          <Card className="relative overflow-hidden bg-gradient-to-br from-navy-600 to-navy-700 p-6 text-white">
            <div className="pointer-events-none absolute -right-10 -top-10 h-40 w-40 rounded-full bg-brand-500/20 blur-2xl" />
            <p className="text-sm text-white/70">Saldo disponivel</p>
            {loading ? (
              <div className="mt-2 h-9 w-40 animate-pulse rounded-lg bg-white/20" />
            ) : (
              <p className="mt-1 text-3xl font-bold tracking-tight">{formatCurrency(wallet?.cashBalance ?? 0)}</p>
            )}
          </Card>

          <Card className="p-5">
            {/* Segmented control */}
            <div className="grid grid-cols-2 gap-1 rounded-xl bg-slate-100 p-1">
              {(['deposit', 'withdraw'] as Mode[]).map((m) => (
                <button
                  key={m}
                  onClick={() => setMode(m)}
                  className={cn(
                    'flex items-center justify-center gap-2 rounded-lg py-2 text-sm font-semibold transition-all',
                    mode === m ? 'bg-white text-navy-700 shadow-soft' : 'text-slate-500 hover:text-slate-700',
                  )}
                >
                  {m === 'deposit' ? <ArrowDownToLine className="h-4 w-4" /> : <ArrowUpFromLine className="h-4 w-4" />}
                  {m === 'deposit' ? 'Depositar' : 'Sacar'}
                </button>
              ))}
            </div>

            <form onSubmit={handleSubmit} className="mt-4 space-y-4">
              <Field
                label="Valor"
                type="number"
                prefix="R$"
                placeholder="0,00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
              />
              <Button
                type="submit"
                fullWidth
                size="lg"
                variant={mode === 'deposit' ? 'primary' : 'secondary'}
                loading={submitting}
              >
                {mode === 'deposit' ? 'Confirmar deposito' : 'Confirmar saque'}
              </Button>
            </form>
          </Card>
        </div>

        {/* Extrato */}
        <Card className="overflow-hidden lg:col-span-2">
          <div className="p-6 pb-2">
            <CardHeader title="Extrato" subtitle="Historico de movimentacoes" />
          </div>
          {loading ? (
            <div className="space-y-2 p-6">
              {[0, 1, 2, 3].map((i) => (
                <div key={i} className="h-12 animate-pulse rounded-xl bg-slate-100" />
              ))}
            </div>
          ) : transactions.length === 0 ? (
            <EmptyState icon={<Receipt className="h-6 w-6" />} title="Nenhuma movimentacao" description="Seus depositos e operacoes aparecerao aqui." />
          ) : (
            <ul className="divide-y divide-slate-100">
              {transactions.map((t) => {
                const isCredit = t.type === 'DEPOSIT' || t.type === 'REDEMPTION';
                return (
                  <li key={t.id} className="flex items-center gap-4 px-6 py-3.5 transition-colors hover:bg-slate-50/60">
                    <span className={cn('flex h-9 w-9 items-center justify-center rounded-full', isCredit ? 'bg-emerald-50' : 'bg-rose-50')}>
                      {txIcon[t.type]}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium text-slate-800">{transactionLabel[t.type]}</p>
                      <p className="truncate text-xs text-slate-400">{t.description ?? formatDate(t.createdAt)}</p>
                    </div>
                    <div className="text-right">
                      <p className={cn('text-sm font-semibold', isCredit ? 'text-emerald-600' : 'text-rose-600')}>
                        {isCredit ? '+' : '−'} {formatCurrency(t.amount)}
                      </p>
                      <p className="text-xs text-slate-400">Saldo {formatCurrency(t.balanceAfter)}</p>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </Card>
      </div>
    </div>
  );
}
