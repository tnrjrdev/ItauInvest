import type { ReactNode } from 'react';
import { LineChart, ShieldCheck, TrendingUp, Wallet } from 'lucide-react';

const highlights = [
  { icon: TrendingUp, title: 'Rentabilidade transparente', text: 'Acompanhe a valorizacao das suas posicoes em tempo real.' },
  { icon: Wallet, title: 'Carteira integrada', text: 'Deposite, aplique e resgate em poucos cliques.' },
  { icon: ShieldCheck, title: 'Seguranca de banco', text: 'Autenticacao JWT e criptografia de ponta a ponta.' },
];

export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen">
      {/* Painel de marca (desktop) */}
      <div className="relative hidden w-1/2 flex-col justify-between overflow-hidden bg-gradient-to-br from-navy-600 via-navy-700 to-navy-900 p-12 text-white lg:flex">
        <div className="pointer-events-none absolute -right-24 -top-24 h-96 w-96 rounded-full bg-brand-500/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-32 -left-16 h-96 w-96 rounded-full bg-brand-500/10 blur-3xl" />

        <div className="relative flex items-center gap-2.5">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-500 shadow-glow">
            <LineChart className="h-6 w-6" />
          </span>
          <span className="text-xl font-extrabold tracking-tight">ItauInvest</span>
        </div>

        <div className="relative">
          <h1 className="max-w-md text-4xl font-extrabold leading-tight">
            Seus investimentos com a solidez de um grande banco.
          </h1>
          <p className="mt-4 max-w-md text-white/70">
            Plataforma completa para construir e acompanhar sua carteira de renda fixa e variavel.
          </p>

          <ul className="mt-10 space-y-5">
            {highlights.map(({ icon: Icon, title, text }) => (
              <li key={title} className="flex items-start gap-4">
                <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white/10">
                  <Icon className="h-5 w-5" />
                </span>
                <div>
                  <p className="font-semibold">{title}</p>
                  <p className="text-sm text-white/60">{text}</p>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <p className="relative text-xs text-white/40">© 2026 ItauInvest · Projeto de referencia</p>
      </div>

      {/* Painel do formulario */}
      <div className="flex w-full items-center justify-center px-4 py-10 sm:px-6 lg:w-1/2">
        <div className="w-full max-w-md animate-slide-up">{children}</div>
      </div>
    </div>
  );
}
