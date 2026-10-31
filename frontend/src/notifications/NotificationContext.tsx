import { Alert, Snackbar } from "@mui/material";
import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";

type NotificationType = "success" | "info" | "error";

interface Notification {
  id: number;
  message: string;
  type: NotificationType;
}

interface NotificationContextValue {
  notify: (message: string, type?: NotificationType) => void;
}

const NotificationContext = createContext<NotificationContextValue | null>(
  null,
);

interface NotificationProviderProps {
  children: ReactNode;
}

export function NotificationProvider({ children }: NotificationProviderProps) {
  const [notification, setNotification] = useState<Notification | null>(null);
  const [open, setOpen] = useState(false);

  const notify = useCallback(
    (message: string, type: NotificationType = "success") => {
      setNotification({ id: Date.now(), message, type });
      setOpen(true);
    },
    [],
  );

  const value = useMemo(() => ({ notify }), [notify]);

  return (
    <NotificationContext value={value}>
      {children}
      <Snackbar
        key={notification?.id}
        open={open}
        autoHideDuration={5000}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
        onClose={(_event, reason) => {
          if (reason !== "clickaway") {
            setOpen(false);
          }
        }}
      >
        <Alert
          severity={notification?.type ?? "success"}
          variant="filled"
          role={notification?.type === "error" ? "alert" : "status"}
          onClose={() => setOpen(false)}
          sx={{ width: "100%" }}
        >
          {notification?.message}
        </Alert>
      </Snackbar>
    </NotificationContext>
  );
}

export function useNotification(): NotificationContextValue {
  const value = useContext(NotificationContext);

  if (!value) {
    throw new Error(
      "useNotification must be used within a NotificationProvider",
    );
  }

  return value;
}
