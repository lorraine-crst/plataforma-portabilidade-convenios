import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",
    contrastThreshold: 4.5,
    primary: {
      main: "#00667f",
    },
    secondary: {
      main: "#121b29",
    },
    success: { main: "#15803d" },
    error: { main: "#b91c1c" },
    warning: { main: "#b45309" },
    background: {
      default: "#f9fafb",
      paper: "#ffffff",
    },
    text: {
      primary: "#111827",
      secondary: "#6b7280",
    },
    divider: "#9ca3af",
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
    button: {
      textTransform: "none",
      fontWeight: 600,
    },
  },
});
