import { Alert } from "@mui/material";
import type { ReactNode } from "react";

type AlertType = "error" | "success" | "info";

interface AppAlertProps {
  type: AlertType;
  children: ReactNode;
}

export function AppAlert({ type, children }: AppAlertProps) {
  return (
    <Alert
      severity={type}
      variant={type === "info" ? "outlined" : "standard"}
      role={type === "error" ? "alert" : "status"}
      sx={
        type === "info"
          ? { bgcolor: "background.paper", borderColor: "divider" }
          : undefined
      }
    >
      {children}
    </Alert>
  );
}
