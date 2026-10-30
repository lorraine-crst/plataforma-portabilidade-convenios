import type {
  Autorizacao,
  FiltrosOperacoes,
  ItemOperacao,
  NovaPortabilidade,
  Operacao,
  Parcela,
  RespostaAutorizacao,
  RespostaCriacaoPortabilidade,
  RespostaLogin,
  Resumo,
} from "../types";
import { request, requestCompleto } from "./client";

export function login(usuario: string, senha: string): Promise<RespostaLogin> {
  return request<RespostaLogin>("/auth/login", {
    metodo: "POST",
    corpo: { usuario, senha },
  });
}

export async function registrarAutorizacao(): Promise<RespostaAutorizacao> {
  const { status, dados } = await requestCompleto<Autorizacao>(
    "/autorizacoes",
    { metodo: "POST" },
  );
  return { ...dados, jaExistia: status === 200 };
}

export function buscarAgenda(beneficiarioId: string): Promise<Parcela[]> {
  return request<Parcela[]>(
    `/beneficiarios/${encodeURIComponent(beneficiarioId)}/agenda`,
  );
}

export function criarPortabilidade(
  dados: NovaPortabilidade,
  chaveIdempotencia: string,
): Promise<RespostaCriacaoPortabilidade> {
  return request<RespostaCriacaoPortabilidade>("/portabilidades", {
    metodo: "POST",
    corpo: dados,
    cabecalhos: { "Idempotency-Key": chaveIdempotencia },
  });
}

export function buscarOperacao(operacaoId: string): Promise<Operacao> {
  return request<Operacao>(`/portabilidades/${encodeURIComponent(operacaoId)}`);
}

export function listarOperacoes(
  filtros: FiltrosOperacoes = {},
): Promise<ItemOperacao[]> {
  const parametros = new URLSearchParams();

  if (filtros.status) {
    parametros.set("status", filtros.status);
  }

  if (filtros.beneficiarioId) {
    parametros.set("beneficiarioId", filtros.beneficiarioId);
  }

  const consulta = parametros.toString();

  return request<ItemOperacao[]>(
    consulta ? `/portabilidades?${consulta}` : "/portabilidades",
  );
}

export function buscarResumo(): Promise<Resumo> {
  return request<Resumo>("/portabilidades/resumo");
}
