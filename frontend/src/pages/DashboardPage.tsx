import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowUpRight, Coins, PiggyBank, TrendingUp, Wallet as WalletIcon } from 'lucide-react';
import { investmentApi } from '@/services/api';
import { extractErrorMessage } from '@/lib/http';
import {
  formatCurrency,
  formatPercent,
  productTypeColor,
  productTypeLabel,
} from '@/lib/format';
import { Button } from '@/components/ui/Button';
import { Card, CardHeader } from '@/components/ui/Card';
import { RiskBadge } from '@/components/ui/Badge';
import { StatCard } from '@/components/ui/StatCard';
import { EmptyState } from '@/components/ui/feedback';
import { DonutChart } from '@/components/ui/DonutChart';
import type { DonutSegment } from '@/components/ui/DonutChart';
import { Modal } from '@/components/ui/Modal';
import { Field } from '@/components/ui/Field';
import { useToast } from '@/components/ui/Toast';
import type { InvestmentPosition, Portfolio } from '@/types';

function StatGrid({ portfolio, loading }: { portfolio: Portfolio | null; loading: boolean }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard
        label="Patrimonio total"
        value={formatCurrency(portfolio?.totalEquity ?? 0)}
        icon={<Coins className="h-5 w-5" />}
        highlight
        loading={loading}
      />
      <StatCard
        label="Saldo em caixa"
        value={formatCurrency(portfolio?.cashBalance ?? 0)}
        icon={<WalletIcon className="h-5 w-5" />}
        iconClass="bg-navy-50 text-navy-600"
        loading={loading}
      />
      <StatCard
        label="Total investido"
        value={formatCurrency(portfolio?.totalInvested ?? 0)}
        icon={<PiggyBank className="h-5 w-5" />}
        iconClass="bg-violet-50 text-violet-600"
        loading={loading}
      />
      <StatCard
        label="Rendimento acumulado"
        value={formatCurrency(portfolio?.totalYield ?? 0)}
        icon={<TrendingUp className="h-5 w-5" />}
        iconClass="bg-emerald-50 text-emerald-600"
        trend={
          portfolio && portfolio.totalInvested > 0
            ? {
                value: `${formatPercent((portfolio.totalYield / portfolio.totalInvested) * 100)} sobre o investido`,
                positive: portfolio.totalYield >= 0,
              }
            : undefined
        }
        loading={loading}
      />
    </div>
  );
}

export default function DashboardPage() {
  const toast = useToast();
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
  const [loading, setLoading] = useState(true);
  const [target, setTarget] = useState<InvestmentPosition | null>(null);
  const [amount, setAmount] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setPortfolio(await investmentApi.portfolio());
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => {
    void load();
  }, [load]);

  const allocation = useMemo<DonutSegment[]>(() => {
    if (!portfolio) return [];
    const byType = new Map<string, number>();
    for (const p of portfolio.positions) {
      byType.set(p.productType, (byType.get(p.productType) ?? 0) + p.grossBalance);
    }
    if (portfolio.cashBalance > 0) byType.set('CAIXA', portfolio.cashBalance);
    return Array.from(byType.entries()).map(([key, value]) => ({
      label: key === 'CAIXA' ? 'Caixa' : productTypeLabel[key] ?? key,
      value,
      color: key === 'CAIXA' ? '#94A3B8' : productTypeColor[key] ?? '#94A3B8',
    }));
  }, [portfolio]);

  const closeModal = () => {
    setTarget(null);
    setAmount('');
  };

  const handleRedeem = async () => {
    if (!target) return;
    const value = Number(amount);
    if (!value || value <= 0) {
      toast.error('Informe um valor de resgate valido.');
      return;
    }
    setSubmitting(true);
    try {
      const updated = await investmentApi.redeem(target.investmentId, value);
      setPortfolio(updated);
      toast.success(`Resgate de ${formatCurrency(value)} realizado.`);
      closeModal();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Minha carteira</h1>
          <p className="text-sm text-slate-500">Posicao consolidada e valorizacao das suas aplicacoes.</p>
        </div>
        <Link to="/products">
          <Button leftIcon={<ArrowUpRight className="h-4 w-4" />}>Investir agora</Button>
        </Link>
      </div>

      <StatGrid portfolio={portfolio} loading={loading} />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Alocacao */}
        <Card className="p-6 lg:col-span-1">
          <CardHeader title="Alocacao" subtitle="Distribuicao do patrimonio" />
          <div className="mt-6">
            {loading ? (
              <div className="flex justify-center py-8">
                <div className="h-44 w-44 animate-pulse rounded-full bg-slate-100" />
              </div>
            ) : allocation.length === 0 ? (
              <EmptyState icon={<PiggyBank className="h-6 w-6" />} title="Sem alocacao" description="Adicione saldo e invista para ver a distribuicao." />
            ) : (
              <DonutChart
                data={allocation}
                centerLabel="Patrimonio"
                centerValue={formatCurrency(portfolio?.totalEquity ?? 0)}
              />
            )}
          </div>
        </Card>

        {/* Posicoes */}
        <Card className="overflow-hidden lg:col-span-2">
          <div className="p-6 pb-0">
            <CardHeader title="Posicoes ativas" subtitle="Investimentos em andamento" />
          </div>
          <div className="mt-4 overflow-x-auto">
            {loading ? (
              <div className="space-y-3 p-6">
                {[0, 1, 2].map((i) => (
                  <div key={i} className="h-14 animate-pulse rounded-xl bg-slate-100" />
                ))}
              </div>
            ) : !portfolio || portfolio.positions.length === 0 ? (
              <EmptyState
                icon={<TrendingUp className="h-6 w-6" />}
                title="Voce ainda nao investiu"
                description="Explore o catalogo de produtos e faca sua primeira aplicacao."
                action={
                  <Link to="/products">
                    <Button size="sm">Ver produtos</Button>
                  </Link>
                }
              />
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-y border-slate-100 bg-slate-50/60 text-left text-xs uppercase tracking-wide text-slate-400">
                    <th className="px-6 py-3 font-medium">Produto</th>
                    <th className="px-3 py-3 font-medium">Investido</th>
                    <th className="px-3 py-3 font-medium">Saldo bruto</th>
                    <th className="px-3 py-3 font-medium">Rendimento</th>
                    <th className="px-6 py-3 text-right font-medium">Acao</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {portfolio.positions.map((p) => (
                    <tr key={p.investmentId} className="transition-colors hover:bg-slate-50/60">
                      <td className="px-6 py-4">
                        <div className="font-medium text-slate-800">{p.productName}</div>
                        <div className="mt-1 flex items-center gap-2">
                          <span className="text-xs text-slate-400">{productTypeLabel[p.productType]}</span>
                          <RiskBadge risk={p.riskLevel} />
                        </div>
                      </td>
                      <td className="px-3 py-4 text-slate-600">{formatCurrency(p.investedAmount)}</td>
                      <td className="px-3 py-4 font-semibold text-slate-900">{formatCurrency(p.grossBalance)}</td>
                      <td className="px-3 py-4">
                        <span className="font-medium text-emerald-600">{formatCurrency(p.grossYield)}</span>
                        <span className="ml-1 text-xs text-emerald-500">{formatPercent(p.yieldPercent)}</span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <Button size="sm" variant="secondary" onClick={() => { setTarget(p); setAmount(''); }}>
                          Resgatar
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </Card>
      </div>

      <Modal
        open={Boolean(target)}
        onClose={closeModal}
        title="Resgatar investimento"
        description={target?.productName}
        footer={
          <>
            <Button variant="secondary" fullWidth onClick={closeModal}>
              Cancelar
            </Button>
            <Button fullWidth loading={submitting} onClick={handleRedeem}>
              Confirmar resgate
            </Button>
          </>
        }
      >
        {target && (
          <div className="space-y-4">
            <div className="flex items-center justify-between rounded-xl bg-slate-50 p-3 text-sm">
              <span className="text-slate-500">Saldo bruto disponivel</span>
              <span className="font-semibold text-slate-900">{formatCurrency(target.grossBalance)}</span>
            </div>
            <Field
              label="Valor a resgatar"
              type="number"
              prefix="R$"
              placeholder="0,00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              autoFocus
            />
            <button
              type="button"
              onClick={() => setAmount(String(target.grossBalance))}
              className="text-xs font-semibold text-brand-600 hover:text-brand-700"
            >
              Resgatar valor total
            </button>
          </div>
        )}
      </Modal>
    </div>
  );
}
