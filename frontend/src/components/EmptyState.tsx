import { Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

interface EmptyStateProps {
  message: string;
  children?: ReactNode;
}

export function EmptyState({ message, children }: EmptyStateProps) {
  return (
    <Stack
      spacing={2}
      sx={{ alignItems: "center", py: 6, textAlign: "center" }}
    >
      <Typography color="textSecondary">{message}</Typography>
      {children}
    </Stack>
  );
}
