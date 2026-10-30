import { CircularProgress, Stack, Typography } from "@mui/material";

interface LoadingProps {
  text?: string;
}

export function Loading({ text = "Carregando…" }: LoadingProps) {
  return (
    <Stack
      direction="row"
      spacing={2}
      role="status"
      sx={{ alignItems: "center", py: 2 }}
    >
      <CircularProgress size={24} aria-hidden="true" />
      <Typography color="text.secondary">{text}</Typography>
    </Stack>
  );
}
