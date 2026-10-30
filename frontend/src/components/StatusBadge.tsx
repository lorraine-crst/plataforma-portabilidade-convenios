import { Chip, type ChipProps } from "@mui/material";

const statusConfig: Record<
  string,
  { label: string; color: ChipProps["color"] }
> = {
  PENDENTE: { label: "Pendente", color: "warning" },
  CONFIRMADA: { label: "Confirmada", color: "success" },
  REJEITADA: { label: "Rejeitada", color: "error" },
  ABERTA: { label: "Aberta", color: "primary" },
};

interface StatusBadgeProps {
  status: string;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const config = statusConfig[status];

  return (
    <Chip
      label={config?.label ?? status}
      color={config?.color ?? "default"}
      size="small"
      variant="outlined"
    />
  );
}
