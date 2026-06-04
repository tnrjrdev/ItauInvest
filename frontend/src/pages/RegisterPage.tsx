import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AtSign, IdCard, Lock, User } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { extractErrorMessage } from '@/lib/http';
import { AuthShell } from '@/components/AuthShell';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { useToast } from '@/components/ui/Toast';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [form, setForm] = useState({ name: '', email: '', cpf: '', password: '' });
  const [loading, setLoading] = useState(false);

  const update = (field: keyof typeof form) => (e: { target: { value: string } }) =>
    setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await register({ ...form, cpf: form.cpf.replace(/\D/g, '') });
      toast.success('Conta criada com sucesso!');
      navigate('/');
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Falha ao cadastrar.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <h2 className="text-2xl font-bold text-slate-900">Crie sua conta</h2>
      <p className="mt-1 text-sm text-slate-500">Comece a investir em poucos minutos.</p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4">
        <Field
          label="Nome completo"
          name="name"
          leftIcon={<User className="h-4 w-4" />}
          value={form.name}
          onChange={update('name')}
          placeholder="Maria Silva"
          required
        />
        <Field
          label="E-mail"
          name="email"
          type="email"
          autoComplete="email"
          leftIcon={<AtSign className="h-4 w-4" />}
          value={form.email}
          onChange={update('email')}
          placeholder="voce@email.com"
          required
        />
        <Field
          label="CPF"
          name="cpf"
          inputMode="numeric"
          leftIcon={<IdCard className="h-4 w-4" />}
          value={form.cpf}
          onChange={update('cpf')}
          placeholder="Somente numeros"
          maxLength={14}
          required
        />
        <Field
          label="Senha"
          name="password"
          type="password"
          autoComplete="new-password"
          leftIcon={<Lock className="h-4 w-4" />}
          value={form.password}
          onChange={update('password')}
          hint="Minimo de 8 caracteres."
          minLength={8}
          required
        />
        <Button type="submit" size="lg" fullWidth loading={loading}>
          Criar conta
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Ja tem conta?{' '}
        <Link to="/login" className="font-semibold text-brand-600 hover:text-brand-700">
          Entrar
        </Link>
      </p>
    </AuthShell>
  );
}
