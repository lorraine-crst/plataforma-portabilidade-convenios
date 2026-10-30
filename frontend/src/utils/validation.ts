import type { DestinationAccount } from "../types";

export type FieldErrors<Field extends string> = Partial<Record<Field, string>>;

export function hasErrors(errors: object): boolean {
  return Object.keys(errors).length > 0;
}

export function validateLogin(
  username: string,
  password: string,
): FieldErrors<"username" | "password"> {
  const errors: FieldErrors<"username" | "password"> = {};

  if (!username.trim()) {
    errors.username = "Informe o usuário";
  }

  if (!password) {
    errors.password = "Informe a senha";
  }

  return errors;
}

export function validateConsent(accepted: boolean): FieldErrors<"consent"> {
  if (!accepted) {
    return { consent: 'Marque "Li e autorizo" para continuar' };
  }

  return {};
}

export function validateBeneficiaryId(
  id: string,
): FieldErrors<"beneficiaryId"> {
  const value = id.trim();

  if (!value) {
    return { beneficiaryId: "Informe o ID do beneficiário" };
  }

  if (!/^[A-Za-z0-9_]+$/.test(value)) {
    return { beneficiaryId: "Use apenas letras, números e _" };
  }

  return {};
}

export function validateDestinationAccount(
  account: DestinationAccount,
): FieldErrors<keyof DestinationAccount> {
  const errors: FieldErrors<keyof DestinationAccount> = {};

  if (!/^\d{3}$/.test(account.banco)) {
    errors.banco = "O banco deve ter 3 dígitos";
  }

  if (!/^\d{4}$/.test(account.agencia)) {
    errors.agencia = "A agência deve ter 4 dígitos";
  }

  if (!account.conta.trim()) {
    errors.conta = "Informe a conta";
  }

  return errors;
}
