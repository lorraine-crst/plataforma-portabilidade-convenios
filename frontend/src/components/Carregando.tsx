import { CircularProgress, Stack, Typography } from "@mui/material";

interface CarregandoProps {
  texto?: string;
}

export function Carregando({ texto = "Carregando…" }: CarregandoProps) {
  return (
    <Stack
      direction="row"
      spacing={2}
      role="status"
      sx={{ alignItems: "center", py: 2 }}
    >
      <CircularProgress size={24} aria-hidden="true" />
      <Typography color="text.secondary">{texto}</Typography>
    </Stack>
  );
}
