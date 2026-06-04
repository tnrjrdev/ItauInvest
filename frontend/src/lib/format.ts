const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

const percentFormatter = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export const formatCurrency = (value: number): string => currencyFormatter.format(value ?? 0);

export const formatPercent = (value: number): string => `${percentFormatter.format(value ?? 0)}%`;

export const formatDate = (iso: string): string =>
  new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

export const initials = (name: string): string =>
  name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('');

export const riskLabel: Record<string, string> = {
  BAIXO: 'Baixo',
  MEDIO: 'Medio',
  ALTO: 'Alto',
};

export const productTypeLabel: Record<string, string> = {
  CDB: 'CDB',
  TESOURO_DIRETO: 'Tesouro Direto',
  LCI_LCA: 'LCI/LCA',
  FUNDO: 'Fundo',
  ACAO: 'Acao',
};

export const transactionLabel: Record<string, string> = {
  DEPOSIT: 'Deposito',
  WITHDRAWAL: 'Saque',
  APPLICATION: 'Aplicacao',
  REDEMPTION: 'Resgate',
};

/** Paleta estavel por tipo de produto (usada no grafico de alocacao). */
export const productTypeColor: Record<string, string> = {
  CDB: '#EC7000',
  TESOURO_DIRETO: '#003399',
  LCI_LCA: '#15A36E',
  FUNDO: '#8B5CF6',
  ACAO: '#E11D48',
};
