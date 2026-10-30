import { Alert } from "@mui/material";
import type { ReactNode } from "react";

type TipoAlerta = "erro" | "sucesso" | "info";

const severidades = {
  erro: "error",
  sucesso: "success",
  info: "info",
} as const;

interface AlertaProps {
  tipo: TipoAlerta;
  children: ReactNode;
}

export function Alerta({ tipo, children }: AlertaProps) {
  return (
    <Alert
      severity={severidades[tipo]}
      role={tipo === "erro" ? "alert" : "status"}
    >
      {children}
    </Alert>
  );
}
