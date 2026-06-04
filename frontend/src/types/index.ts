export type Role = 'ADMIN' | 'CLIENT';

export type ProductType = 'CDB' | 'TESOURO_DIRETO' | 'LCI_LCA' | 'FUNDO' | 'ACAO';
export type RiskLevel = 'BAIXO' | 'MEDIO' | 'ALTO';
export type InvestmentStatus = 'ACTIVE' | 'REDEEMED';
export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'APPLICATION' | 'REDEMPTION';

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  name: string;
  role: Role;
}

export interface Product {
  id: string;
  name: string;
  type: ProductType;
  riskLevel: RiskLevel;
  annualRatePercent: number;
  minimumAmount: number;
  liquidityDays: number;
  maturityDate: string | null;
  active: boolean;
}

export interface Wallet {
  id: string;
  userId: string;
  cashBalance: number;
}

export interface InvestmentPosition {
  investmentId: string;
  productId: string;
  productName: string;
  productType: ProductType;
  riskLevel: RiskLevel;
  annualRatePercent: number;
  investedAmount: number;
  grossBalance: number;
  grossYield: number;
  yieldPercent: number;
  status: InvestmentStatus;
  appliedAt: string;
}

export interface Portfolio {
  cashBalance: number;
  totalInvested: number;
  totalGrossBalance: number;
  totalYield: number;
  totalEquity: number;
  positions: InvestmentPosition[];
}

export interface Transaction {
  id: string;
  type: TransactionType;
  amount: number;
  balanceAfter: number;
  description: string | null;
  investmentId: string | null;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: { field: string; message: string }[];
}
