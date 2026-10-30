export type Perfil = "BENEFICIARIO" | "OPERADOR";

export type StatusOperacao = "PENDENTE" | "CONFIRMADA" | "REJEITADA";

export type CodigoErro =
  | "CREDENCIAIS_INVALIDAS"
  | "SESSAO_EXPIRADA"
  | "ACESSO_NEGADO"
  | "NAO_ENCONTRADO"
  | "CONFLITO"
  | "BENEFICIARIO_NAO_AUTORIZADO"
  | "CENTRAL_INDISPONIVEL"
  | "VALIDACAO"
  | "SEM_CONEXAO";

export interface Usuario {
  nome: string;
  perfil: Perfil;
}

export interface RespostaLogin extends Usuario {
  token: string;
}

export interface Autorizacao {
  autorizacaoId: string;
  autorizadaEm: string;
}

export interface RespostaAutorizacao extends Autorizacao {
  jaExistia: boolean;
}

export interface Parcela {
  parcelaId: string;
  convenioId: string;
  valor: number;
  dataDesconto: string;
  status: string;
}

export interface ContaDestino {
  banco: string;
  agencia: string;
  conta: string;
}

export interface NovaPortabilidade {
  beneficiarioId: string;
  parcelaIds: string[];
  contaDestino: ContaDestino;
}

export interface RespostaCriacaoPortabilidade {
  operacaoId: string;
  status: StatusOperacao;
}

export interface EventoOperacao {
  status: StatusOperacao;
  ocorridoEm: string;
  descricao: string;
}

export interface Operacao {
  operacaoId: string;
  beneficiarioId: string;
  status: StatusOperacao;
  motivoRejeicao: string | null;
  criadaEm: string;
  valorTotal: number;
  parcelas: Parcela[];
  contaDestino: ContaDestino;
  eventos: EventoOperacao[];
}

export interface ItemOperacao {
  operacaoId: string;
  beneficiarioId: string;
  status: StatusOperacao;
  motivoRejeicao: string | null;
  criadaEm: string;
  quantidadeParcelas: number;
  valorTotal: number;
}

export interface FiltrosOperacoes {
  status?: StatusOperacao;
  beneficiarioId?: string;
}

export interface Resumo {
  total: number;
  pendentes: number;
  confirmadas: number;
  rejeitadas: number;
  valorTotalConfirmado: number;
}

export interface RespostaErro {
  codigo: CodigoErro;
  mensagem: string;
}
