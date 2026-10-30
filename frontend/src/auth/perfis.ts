import type { Perfil } from "../types";

export const nomesDosPerfis: Record<Perfil, string> = {
  BENEFICIARIO: "Beneficiário",
  OPERADOR: "Operador",
};

export function homeDoPerfil(perfil: Perfil): string {
  return perfil === "BENEFICIARIO"
    ? "/beneficiario/autorizar"
    : "/operador/portabilidade";
}
