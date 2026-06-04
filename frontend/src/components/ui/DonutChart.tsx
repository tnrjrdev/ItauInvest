export interface DonutSegment {
  label: string;
  value: number;
  color: string;
}

interface DonutChartProps {
  data: DonutSegment[];
  centerLabel: string;
  centerValue: string;
}

// Circunferencia normalizada para 100 (r = 100 / 2PI).
const RADIUS = 15.915494;

export function DonutChart({ data, centerLabel, centerValue }: DonutChartProps) {
  const total = data.reduce((sum, s) => sum + s.value, 0);
  let cumulative = 0;

  return (
    <div className="flex flex-col items-center gap-6 sm:flex-row sm:items-center sm:gap-8">
      <div className="relative h-44 w-44 shrink-0">
        <svg viewBox="0 0 36 36" className="h-full w-full -rotate-90">
          <circle cx="18" cy="18" r={RADIUS} fill="none" stroke="#F1F5F9" strokeWidth="3.5" />
          {total > 0 &&
            data.map((segment) => {
              const pct = (segment.value / total) * 100;
              const dash = `${pct} ${100 - pct}`;
              const offset = 100 - cumulative;
              cumulative += pct;
              return (
                <circle
                  key={segment.label}
                  cx="18"
                  cy="18"
                  r={RADIUS}
                  fill="none"
                  stroke={segment.color}
                  strokeWidth="3.5"
                  strokeDasharray={dash}
                  strokeDashoffset={offset}
                  strokeLinecap="round"
                  className="transition-all duration-500"
                />
              );
            })}
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <span className="text-xs font-medium text-slate-400">{centerLabel}</span>
          <span className="text-lg font-bold text-slate-900">{centerValue}</span>
        </div>
      </div>

      <ul className="w-full space-y-2">
        {data.map((segment) => {
          const pct = total > 0 ? (segment.value / total) * 100 : 0;
          return (
            <li key={segment.label} className="flex items-center gap-3 text-sm">
              <span className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ background: segment.color }} />
              <span className="flex-1 text-slate-600">{segment.label}</span>
              <span className="font-semibold text-slate-900">{pct.toFixed(1)}%</span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
