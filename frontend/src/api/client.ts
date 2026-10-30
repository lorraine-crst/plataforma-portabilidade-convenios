import type { ErrorResponse } from "../types";
import { ApiError } from "./ApiError";

const DEFAULT_TIMEOUT_MS = 10000;

let authToken: string | null = null;
let onSessionExpired: (() => void) | null = null;

export function setAuthToken(token: string | null): void {
  authToken = token;
}

export function setOnSessionExpired(callback: (() => void) | null): void {
  onSessionExpired = callback;
}

interface RequestOptions {
  method?: "GET" | "POST";
  body?: unknown;
  headers?: Record<string, string>;
  timeoutMs?: number;
}

export interface ResponseWithStatus<T> {
  status: number;
  data: T;
}

function isErrorResponse(value: unknown): value is ErrorResponse {
  return (
    typeof value === "object" &&
    value !== null &&
    "codigo" in value &&
    "mensagem" in value
  );
}

async function readBody(response: Response): Promise<unknown> {
  const text = await response.text();

  if (!text) {
    return undefined;
  }

  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}

export async function requestWithStatus<T>(
  path: string,
  options: RequestOptions = {},
): Promise<ResponseWithStatus<T>> {
  const {
    method = "GET",
    body,
    headers: extraHeaders = {},
    timeoutMs = DEFAULT_TIMEOUT_MS,
  } = options;

  const headers: Record<string, string> = {
    Accept: "application/json",
    ...extraHeaders,
  };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

  let response: Response;

  try {
    response = await fetch(`/api${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: controller.signal,
    });
  } catch {
    const message = controller.signal.aborted
      ? "O servidor demorou para responder. Tente novamente."
      : "Não foi possível conectar ao servidor. Verifique sua conexão.";
    throw new ApiError("SEM_CONEXAO", message, 0);
  } finally {
    clearTimeout(timeoutId);
  }

  const data = await readBody(response);

  if (!response.ok) {
    const error: ErrorResponse = isErrorResponse(data)
      ? data
      : {
          codigo: "SEM_CONEXAO",
          mensagem: "Não foi possível falar com o servidor. Tente novamente.",
        };

    if (error.codigo === "SESSAO_EXPIRADA") {
      onSessionExpired?.();
    }

    throw new ApiError(error.codigo, error.mensagem, response.status);
  }

  return { status: response.status, data: data as T };
}

export async function request<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { data } = await requestWithStatus<T>(path, options);
  return data;
}
