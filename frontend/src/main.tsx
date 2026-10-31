import { CssBaseline, ThemeProvider } from "@mui/material";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router";
import { App } from "./App";
import { AuthProvider } from "./auth/AuthContext";
import { NotificationProvider } from "./notifications/NotificationContext";
import { theme } from "./theme";

const root = document.getElementById("root");

if (!root) {
  throw new Error("Element #root not found");
}

createRoot(root).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <NotificationProvider>
        <BrowserRouter>
          <AuthProvider>
            <App />
          </AuthProvider>
        </BrowserRouter>
      </NotificationProvider>
    </ThemeProvider>
  </StrictMode>,
);
