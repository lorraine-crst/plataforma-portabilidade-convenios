export type Role = "BENEFICIARIO" | "OPERADOR";

export type OperationStatus = "PENDENTE" | "CONFIRMADA" | "REJEITADA";

export type ErrorCode =
  | "CREDENCIAIS_INVALIDAS"
  | "SESSAO_EXPIRADA"
  | "ACESSO_NEGADO"
  | "NAO_ENCONTRADO"
  | "CONFLITO"
  | "BENEFICIARIO_NAO_AUTORIZADO"
  | "CENTRAL_INDISPONIVEL"
  | "VALIDACAO"
  | "SEM_CONEXAO";

export interface User {
  nome: string;
  perfil: Role;
}

export interface LoginResponse extends User {
  token: string;
}

export interface Authorization {
  autorizacaoId: string;
  autorizadaEm: string;
}

export interface AuthorizationResult extends Authorization {
  alreadyExisted: boolean;
}

export interface Installment {
  parcelaId: string;
  convenioId: string;
  valor: number;
  dataDesconto: string;
  status: string;
}

export interface DestinationAccount {
  banco: string;
  agencia: string;
  conta: string;
}

export interface NewPortability {
  beneficiarioId: string;
  parcelaIds: string[];
  contaDestino: DestinationAccount;
}

export interface CreatePortabilityResponse {
  operacaoId: string;
  status: OperationStatus;
}

export interface OperationEvent {
  status: OperationStatus;
  ocorridoEm: string;
  descricao: string;
}

export interface Operation {
  operacaoId: string;
  beneficiarioId: string;
  status: OperationStatus;
  motivoRejeicao: string | null;
  criadaEm: string;
  valorTotal: number;
  parcelas: Installment[];
  contaDestino: DestinationAccount;
  eventos: OperationEvent[];
}

export interface OperationListItem {
  operacaoId: string;
  beneficiarioId: string;
  status: OperationStatus;
  motivoRejeicao: string | null;
  criadaEm: string;
  quantidadeParcelas: number;
  valorTotal: number;
}

export interface OperationFilters {
  status?: OperationStatus;
  beneficiarioId?: string;
}

export interface Summary {
  total: number;
  pendentes: number;
  confirmadas: number;
  rejeitadas: number;
  valorTotalConfirmado: number;
}

export interface ErrorResponse {
  codigo: ErrorCode;
  mensagem: string;
}
