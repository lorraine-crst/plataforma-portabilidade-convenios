import type { RespostaErro } from "../types";
import { ApiError } from "./ApiError";

const TIMEOUT_PADRAO_MS = 10000;

let token: string | null = null;
let aoExpirarSessao: (() => void) | null = null;

export function definirToken(novoToken: string | null): void {
  token = novoToken;
}

export function definirAoExpirarSessao(funcao: (() => void) | null): void {
  aoExpirarSessao = funcao;
}

interface OpcoesRequisicao {
  metodo?: "GET" | "POST";
  corpo?: unknown;
  cabecalhos?: Record<string, string>;
  timeoutMs?: number;
}

export interface RespostaCompleta<T> {
  status: number;
  dados: T;
}

function temFormatoDeErro(valor: unknown): valor is RespostaErro {
  return (
    typeof valor === "object" &&
    valor !== null &&
    "codigo" in valor &&
    "mensagem" in valor
  );
}

async function lerCorpo(resposta: Response): Promise<unknown> {
  const texto = await resposta.text();

  if (!texto) {
    return undefined;
  }

  try {
    return JSON.parse(texto);
  } catch {
    return undefined;
  }
}

export async function requestCompleto<T>(
  caminho: string,
  opcoes: OpcoesRequisicao = {},
): Promise<RespostaCompleta<T>> {
  const {
    metodo = "GET",
    corpo,
    cabecalhos = {},
    timeoutMs = TIMEOUT_PADRAO_MS,
  } = opcoes;

  const headers: Record<string, string> = {
    Accept: "application/json",
    ...cabecalhos,
  };

  if (corpo !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const controlador = new AbortController();
  const temporizador = setTimeout(() => controlador.abort(), timeoutMs);

  let resposta: Response;

  try {
    resposta = await fetch(`/api${caminho}`, {
      method: metodo,
      headers,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
      signal: controlador.signal,
    });
  } catch {
    const mensagem = controlador.signal.aborted
      ? "O servidor demorou para responder. Tente novamente."
      : "Não foi possível conectar ao servidor. Verifique sua conexão.";
    throw new ApiError("SEM_CONEXAO", mensagem, 0);
  } finally {
    clearTimeout(temporizador);
  }

  const dados = await lerCorpo(resposta);

  if (!resposta.ok) {
    const erro: RespostaErro = temFormatoDeErro(dados)
      ? dados
      : {
          codigo: "SEM_CONEXAO",
          mensagem: "Não foi possível falar com o servidor. Tente novamente.",
        };

    if (erro.codigo === "SESSAO_EXPIRADA") {
      aoExpirarSessao?.();
    }

    throw new ApiError(erro.codigo, erro.mensagem, resposta.status);
  }

  return { status: resposta.status, dados: dados as T };
}

export async function request<T>(
  caminho: string,
  opcoes: OpcoesRequisicao = {},
): Promise<T> {
  const { dados } = await requestCompleto<T>(caminho, opcoes);
  return dados;
}
