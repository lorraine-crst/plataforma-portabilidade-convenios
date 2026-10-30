import { Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

interface VazioProps {
  mensagem: string;
  children?: ReactNode;
}

export function Vazio({ mensagem, children }: VazioProps) {
  return (
    <Stack
      spacing={2}
      sx={{ alignItems: "center", py: 6, textAlign: "center" }}
    >
      <Typography color="text.secondary">{mensagem}</Typography>
      {children}
    </Stack>
  );
}
