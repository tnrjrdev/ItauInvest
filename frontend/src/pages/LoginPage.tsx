import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AtSign, Lock, LineChart } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { extractErrorMessage } from '@/lib/http';
import { AuthShell } from '@/components/AuthShell';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { useToast } from '@/components/ui/Toast';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [email, setEmail] = useState('cliente@itau.com.br');
  const [password, setPassword] = useState('Cliente@123');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Falha ao autenticar.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <div className="mb-8 lg:hidden">
        <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand-500 text-white shadow-glow">
          <LineChart className="h-6 w-6" />
        </span>
      </div>
      <h2 className="text-2xl font-bold text-slate-900">Bem-vindo de volta</h2>
      <p className="mt-1 text-sm text-slate-500">Acesse sua carteira de investimentos.</p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4">
        <Field
          label="E-mail"
          name="email"
          type="email"
          autoComplete="email"
          leftIcon={<AtSign className="h-4 w-4" />}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <Field
          label="Senha"
          name="password"
          type="password"
          autoComplete="current-password"
          leftIcon={<Lock className="h-4 w-4" />}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Button type="submit" size="lg" fullWidth loading={loading}>
          Entrar
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Nao tem conta?{' '}
        <Link to="/register" className="font-semibold text-brand-600 hover:text-brand-700">
          Cadastre-se gratuitamente
        </Link>
      </p>

      <div className="mt-8 rounded-xl border border-dashed border-slate-200 bg-slate-50 p-3 text-center text-xs text-slate-500">
        <strong className="text-slate-600">Conta demo:</strong> cliente@itau.com.br · Cliente@123
      </div>
    </AuthShell>
  );
}
