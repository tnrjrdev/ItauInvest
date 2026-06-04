import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';
import { riskLabel } from '@/lib/format';

type Tone = 'neutral' | 'brand' | 'navy' | 'success' | 'warning' | 'danger';

const tones: Record<Tone, string> = {
  neutral: 'bg-slate-100 text-slate-600',
  brand: 'bg-brand-50 text-brand-700',
  navy: 'bg-navy-50 text-navy-600',
  success: 'bg-emerald-50 text-emerald-700',
  warning: 'bg-amber-50 text-amber-700',
  danger: 'bg-rose-50 text-rose-700',
};

export function Badge({
  tone = 'neutral',
  children,
  className,
}: {
  tone?: Tone;
  children: ReactNode;
  className?: string;
}) {
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold',
        tones[tone],
        className,
      )}
    >
      {children}
    </span>
  );
}

export function RiskBadge({ risk }: { risk: string }) {
  const tone: Tone = risk === 'BAIXO' ? 'success' : risk === 'MEDIO' ? 'warning' : 'danger';
  return (
    <Badge tone={tone}>
      <span className="h-1.5 w-1.5 rounded-full bg-current" />
      Risco {riskLabel[risk] ?? risk}
    </Badge>
  );
}
