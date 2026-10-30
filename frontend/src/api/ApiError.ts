import type { CodigoErro } from "../types";

export class ApiError extends Error {
  readonly codigo: CodigoErro;
  readonly status: number;

  constructor(codigo: CodigoErro, mensagem: string, status: number) {
    super(mensagem);
    this.name = "ApiError";
    this.codigo = codigo;
    this.status = status;
  }
}
