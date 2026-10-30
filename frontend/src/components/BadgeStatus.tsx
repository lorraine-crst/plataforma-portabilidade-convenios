import { Chip, type ChipProps } from "@mui/material";

const configuracoes: Record<
  string,
  { rotulo: string; cor: ChipProps["color"] }
> = {
  PENDENTE: { rotulo: "Pendente", cor: "warning" },
  CONFIRMADA: { rotulo: "Confirmada", cor: "success" },
  REJEITADA: { rotulo: "Rejeitada", cor: "error" },
  ABERTA: { rotulo: "Aberta", cor: "primary" },
};

interface BadgeStatusProps {
  status: string;
}

export function BadgeStatus({ status }: BadgeStatusProps) {
  const configuracao = configuracoes[status];

  return (
    <Chip
      label={configuracao?.rotulo ?? status}
      color={configuracao?.cor ?? "default"}
      size="small"
      variant="outlined"
    />
  );
}
