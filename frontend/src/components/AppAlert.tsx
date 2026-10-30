import { Alert } from "@mui/material";
import type { ReactNode } from "react";

type AlertType = "error" | "success" | "info";

interface AppAlertProps {
  type: AlertType;
  children: ReactNode;
}

export function AppAlert({ type, children }: AppAlertProps) {
  return (
    <Alert severity={type} role={type === "error" ? "alert" : "status"}>
      {children}
    </Alert>
  );
}
