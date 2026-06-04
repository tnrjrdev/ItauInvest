import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';
import { Skeleton } from './feedback';

interface StatCardProps {
  label: string;
  value: string;
  icon: ReactNode;
  iconClass?: string;
  trend?: { value: string; positive: boolean };
  highlight?: boolean;
  loading?: boolean;
}

export function StatCard({ label, value, icon, iconClass, trend, highlight, loading }: StatCardProps) {
  return (
    <div
      className={cn(
        'card relative overflow-hidden p-5',
        highlight && 'bg-gradient-to-br from-navy-600 to-navy-700 text-white border-transparent shadow-card-hover',
      )}
    >
      <div className="flex items-start justify-between">
        <p className={cn('text-sm font-medium', highlight ? 'text-white/70' : 'text-slate-500')}>
          {label}
        </p>
        <span
          className={cn(
            'flex h-9 w-9 items-center justify-center rounded-xl',
            highlight ? 'bg-white/15 text-white' : iconClass ?? 'bg-brand-50 text-brand-600',
          )}
        >
          {icon}
        </span>
      </div>
      {loading ? (
        <Skeleton className={cn('mt-3 h-7 w-32', highlight && 'bg-white/20')} />
      ) : (
        <p className={cn('mt-2 text-2xl font-bold tracking-tight', highlight ? 'text-white' : 'text-slate-900')}>
          {value}
        </p>
      )}
      {trend && !loading && (
        <p
          className={cn(
            'mt-1 text-xs font-medium',
            highlight ? 'text-white/80' : trend.positive ? 'text-emerald-600' : 'text-rose-600',
          )}
        >
          {trend.value}
        </p>
      )}
    </div>
  );
}
