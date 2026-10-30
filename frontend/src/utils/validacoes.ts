import type { ContaDestino } from "../types";

export type Erros<Campo extends string> = Partial<Record<Campo, string>>;

export function temErros(erros: object): boolean {
  return Object.keys(erros).length > 0;
}

export function validarLogin(
  usuario: string,
  senha: string,
): Erros<"usuario" | "senha"> {
  const erros: Erros<"usuario" | "senha"> = {};

  if (!usuario.trim()) {
    erros.usuario = "Informe o usuário";
  }

  if (!senha) {
    erros.senha = "Informe a senha";
  }

  return erros;
}

export function validarAceite(aceito: boolean): Erros<"aceite"> {
  if (!aceito) {
    return { aceite: 'Marque "Li e autorizo" para continuar' };
  }

  return {};
}

export function validarBeneficiarioId(id: string): Erros<"beneficiarioId"> {
  const valor = id.trim();

  if (!valor) {
    return { beneficiarioId: "Informe o ID do beneficiário" };
  }

  if (!/^[A-Za-z0-9_]+$/.test(valor)) {
    return { beneficiarioId: "Use apenas letras, números e _" };
  }

  return {};
}

export function validarContaDestino(
  conta: ContaDestino,
): Erros<keyof ContaDestino> {
  const erros: Erros<keyof ContaDestino> = {};

  if (!/^\d{3}$/.test(conta.banco)) {
    erros.banco = "O banco deve ter 3 dígitos";
  }

  if (!/^\d{4}$/.test(conta.agencia)) {
    erros.agencia = "A agência deve ter 4 dígitos";
  }

  if (!conta.conta.trim()) {
    erros.conta = "Informe a conta";
  }

  return erros;
}
