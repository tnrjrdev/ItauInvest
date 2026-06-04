import { useCallback, useEffect, useMemo, useState } from 'react';
import { Clock, PackageOpen, TrendingUp } from 'lucide-react';
import { investmentApi, productApi } from '@/services/api';
import { extractErrorMessage } from '@/lib/http';
import { formatCurrency, formatPercent, productTypeLabel } from '@/lib/format';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Badge, RiskBadge } from '@/components/ui/Badge';
import { EmptyState } from '@/components/ui/feedback';
import { Modal } from '@/components/ui/Modal';
import { Field } from '@/components/ui/Field';
import { useToast } from '@/components/ui/Toast';
import { cn } from '@/lib/cn';
import type { Product, ProductType } from '@/types';

const filters: { label: string; value: ProductType | 'ALL' }[] = [
  { label: 'Todos', value: 'ALL' },
  { label: 'CDB', value: 'CDB' },
  { label: 'Tesouro', value: 'TESOURO_DIRETO' },
  { label: 'LCI/LCA', value: 'LCI_LCA' },
  { label: 'Fundos', value: 'FUNDO' },
  { label: 'Acoes', value: 'ACAO' },
];

function ProductCardSkeleton() {
  return (
    <Card className="space-y-4 p-5">
      <div className="skeleton h-5 w-3/4" />
      <div className="skeleton h-4 w-1/3" />
      <div className="space-y-2 pt-2">
        <div className="skeleton h-4 w-full" />
        <div className="skeleton h-4 w-full" />
      </div>
      <div className="skeleton h-11 w-full rounded-xl" />
    </Card>
  );
}

export default function ProductsPage() {
  const toast = useToast();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<ProductType | 'ALL'>('ALL');
  const [target, setTarget] = useState<Product | null>(null);
  const [amount, setAmount] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const page = await productApi.list();
      setProducts(page.content);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => {
    void load();
  }, [load]);

  const visible = useMemo(
    () => (filter === 'ALL' ? products : products.filter((p) => p.type === filter)),
    [products, filter],
  );

  const closeModal = () => {
    setTarget(null);
    setAmount('');
  };

  const handleApply = async () => {
    if (!target) return;
    const value = Number(amount);
    if (!value || value < target.minimumAmount) {
      toast.error(`O valor minimo e ${formatCurrency(target.minimumAmount)}.`);
      return;
    }
    setSubmitting(true);
    try {
      await investmentApi.apply(target.id, value);
      toast.success(`Aplicacao de ${formatCurrency(value)} em ${target.name} realizada.`);
      closeModal();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Produtos de investimento</h1>
        <p className="text-sm text-slate-500">Escolha um produto e aplique seu saldo em caixa.</p>
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap gap-2">
        {filters.map((f) => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
            className={cn(
              'rounded-full px-4 py-1.5 text-sm font-medium transition-colors',
              filter === f.value
                ? 'bg-navy-600 text-white shadow-soft'
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50',
            )}
          >
            {f.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-3">
          {[0, 1, 2, 3, 4, 5].map((i) => (
            <ProductCardSkeleton key={i} />
          ))}
        </div>
      ) : visible.length === 0 ? (
        <Card>
          <EmptyState icon={<PackageOpen className="h-6 w-6" />} title="Nenhum produto encontrado" description="Tente outro filtro." />
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-3">
          {visible.map((product) => (
            <Card key={product.id} hover className="flex flex-col p-5">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <Badge tone="brand">{productTypeLabel[product.type]}</Badge>
                  <h3 className="mt-2 font-semibold leading-snug text-slate-900">{product.name}</h3>
                </div>
                <RiskBadge risk={product.riskLevel} />
              </div>

              <div className="my-4 rounded-xl bg-emerald-50/70 p-3 text-center">
                <p className="text-xs font-medium text-emerald-600">Rentabilidade</p>
                <p className="text-xl font-bold text-emerald-700">
                  {formatPercent(product.annualRatePercent)}
                  <span className="text-sm font-medium"> a.a.</span>
                </p>
              </div>

              <dl className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <dt className="text-slate-500">Aplicacao minima</dt>
                  <dd className="font-medium text-slate-700">{formatCurrency(product.minimumAmount)}</dd>
                </div>
                <div className="flex items-center justify-between">
                  <dt className="flex items-center gap-1 text-slate-500">
                    <Clock className="h-3.5 w-3.5" /> Liquidez
                  </dt>
                  <dd className="font-medium text-slate-700">D+{product.liquidityDays}</dd>
                </div>
              </dl>

              <div className="mt-5 pt-1">
                <Button fullWidth leftIcon={<TrendingUp className="h-4 w-4" />} onClick={() => { setTarget(product); setAmount(''); }}>
                  Investir
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(target)}
        onClose={closeModal}
        title="Aplicar em produto"
        description={target?.name}
        footer={
          <>
            <Button variant="secondary" fullWidth onClick={closeModal}>
              Cancelar
            </Button>
            <Button fullWidth loading={submitting} onClick={handleApply}>
              Confirmar aplicacao
            </Button>
          </>
        }
      >
        {target && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div className="rounded-xl bg-slate-50 p-3">
                <p className="text-xs text-slate-500">Rentabilidade</p>
                <p className="font-semibold text-emerald-600">{formatPercent(target.annualRatePercent)} a.a.</p>
              </div>
              <div className="rounded-xl bg-slate-50 p-3">
                <p className="text-xs text-slate-500">Minimo</p>
                <p className="font-semibold text-slate-800">{formatCurrency(target.minimumAmount)}</p>
              </div>
            </div>
            <Field
              label="Valor a aplicar"
              type="number"
              prefix="R$"
              placeholder="0,00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              hint={`Liquidez D+${target.liquidityDays}.`}
              autoFocus
            />
          </div>
        )}
      </Modal>
    </div>
  );
}
