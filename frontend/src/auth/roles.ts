import type { Role } from "../types";

export const roleLabels: Record<Role, string> = {
  BENEFICIARIO: "Beneficiário",
  OPERADOR: "Operador",
};

export function getHomePath(role: Role): string {
  return role === "BENEFICIARIO"
    ? "/beneficiario/solicitacoes"
    : "/operador/portabilidade";
}
