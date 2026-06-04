import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { LayoutDashboard, LineChart, LogOut, Menu, Wallet, X } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { initials } from '@/lib/format';
import { cn } from '@/lib/cn';

interface NavItem {
  to: string;
  label: string;
  icon: LucideIcon;
  end?: boolean;
}

const navItems: NavItem[] = [
  { to: '/', label: 'Carteira', icon: LayoutDashboard, end: true },
  { to: '/products', label: 'Produtos', icon: LineChart },
  { to: '/wallet', label: 'Caixa', icon: Wallet },
];

function BrandMark() {
  return (
    <div className="flex items-center gap-2.5">
      <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-500 text-white shadow-glow">
        <LineChart className="h-5 w-5" />
      </span>
      <span className="text-lg font-extrabold tracking-tight">
        <span className="text-brand-500">Itau</span>
        <span className="text-navy-600">Invest</span>
      </span>
    </div>
  );
}

function NavLinks({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <nav className="space-y-1">
      {navItems.map(({ to, label, icon: Icon, end }) => (
        <NavLink
          key={to}
          to={to}
          end={end}
          onClick={onNavigate}
          className={({ isActive }) =>
            cn(
              'flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium transition-colors',
              isActive
                ? 'bg-brand-50 text-brand-700'
                : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
            )
          }
        >
          {({ isActive }) => (
            <>
              <Icon className={cn('h-5 w-5', isActive ? 'text-brand-500' : 'text-slate-400')} />
              {label}
            </>
          )}
        </NavLink>
      ))}
    </nav>
  );
}

function UserCard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  if (!user) return null;

  return (
    <div className="rounded-xl border border-slate-200 bg-slate-50/80 p-3">
      <div className="flex items-center gap-3">
        <span className="flex h-9 w-9 items-center justify-center rounded-full bg-navy-600 text-sm font-semibold text-white">
          {initials(user.name)}
        </span>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold text-slate-800">{user.name}</p>
          <p className="text-xs text-slate-400">{user.role === 'ADMIN' ? 'Administrador' : 'Cliente'}</p>
        </div>
        <button
          onClick={() => {
            logout();
            navigate('/login');
          }}
          className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-white hover:text-rose-600"
          aria-label="Sair"
          title="Sair"
        >
          <LogOut className="h-4 w-4" />
        </button>
      </div>
    </div>
  );
}

export default function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Sidebar fixa (desktop) */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r border-slate-200 bg-white p-4 lg:flex">
        <div className="px-2 py-3">
          <BrandMark />
        </div>
        <div className="mt-6 flex-1">
          <NavLinks />
        </div>
        <UserCard />
      </aside>

      {/* Drawer mobile */}
      {mobileOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-navy-900/40 backdrop-blur-sm animate-fade-in" onClick={() => setMobileOpen(false)} />
          <aside className="absolute inset-y-0 left-0 flex w-72 max-w-[80%] flex-col bg-white p-4 shadow-card-hover animate-slide-in-right">
            <div className="flex items-center justify-between px-2 py-3">
              <BrandMark />
              <button onClick={() => setMobileOpen(false)} className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100">
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="mt-6 flex-1">
              <NavLinks onNavigate={() => setMobileOpen(false)} />
            </div>
            <UserCard />
          </aside>
        </div>
      )}

      {/* Conteudo */}
      <div className="lg:pl-64">
        {/* Topbar mobile */}
        <header className="sticky top-0 z-20 flex items-center justify-between border-b border-slate-200 bg-white/80 px-4 py-3 backdrop-blur lg:hidden">
          <button onClick={() => setMobileOpen(true)} className="rounded-lg p-2 text-slate-600 hover:bg-slate-100">
            <Menu className="h-5 w-5" />
          </button>
          <BrandMark />
          <div className="w-9" />
        </header>

        <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6 lg:px-8 lg:py-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
