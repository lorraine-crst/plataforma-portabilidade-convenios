import type {
  Authorization,
  AuthorizationResult,
  CreatePortabilityResponse,
  Installment,
  LoginResponse,
  NewPortability,
  Operation,
  OperationFilters,
  OperationListItem,
  Summary,
} from "../types";
import { request, requestWithStatus } from "./client";

export function login(
  username: string,
  password: string,
): Promise<LoginResponse> {
  return request<LoginResponse>("/auth/login", {
    method: "POST",
    body: { usuario: username, senha: password },
  });
}

export async function registerAuthorization(): Promise<AuthorizationResult> {
  const { status, data } = await requestWithStatus<Authorization>(
    "/autorizacoes",
    { method: "POST" },
  );
  return { ...data, alreadyExisted: status === 200 };
}

export function fetchSchedule(beneficiarioId: string): Promise<Installment[]> {
  return request<Installment[]>(
    `/beneficiarios/${encodeURIComponent(beneficiarioId)}/agenda`,
  );
}

export function createPortability(
  portability: NewPortability,
  idempotencyKey: string,
): Promise<CreatePortabilityResponse> {
  return request<CreatePortabilityResponse>("/portabilidades", {
    method: "POST",
    body: portability,
    headers: { "Idempotency-Key": idempotencyKey },
  });
}

export function fetchOperation(operacaoId: string): Promise<Operation> {
  return request<Operation>(
    `/portabilidades/${encodeURIComponent(operacaoId)}`,
  );
}

export function listOperations(
  filters: OperationFilters = {},
): Promise<OperationListItem[]> {
  const params = new URLSearchParams();

  if (filters.status) {
    params.set("status", filters.status);
  }

  if (filters.beneficiarioId) {
    params.set("beneficiarioId", filters.beneficiarioId);
  }

  const query = params.toString();

  return request<OperationListItem[]>(
    query ? `/portabilidades?${query}` : "/portabilidades",
  );
}

export function fetchSummary(): Promise<Summary> {
  return request<Summary>("/portabilidades/resumo");
}
