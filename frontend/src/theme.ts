import { createTheme, type PaletteColor } from "@mui/material/styles";

declare module "@mui/material/styles" {
  interface Palette {
    brown: PaletteColor;
  }

  interface PaletteOptions {
    brown?: PaletteColor;
  }

  interface TypeText {
    onDark: string;
  }
}

declare module "@mui/material/Button" {
  interface ButtonPropsColorOverrides {
    brown: true;
  }
}

declare module "@mui/material/TextField" {
  interface TextFieldPropsColorOverrides {
    brown: true;
  }
}

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
    brown: {
      main: "#3d2f25",
      light: "#5c4331",
      dark: "#2e231b",
      contrastText: "#ffffff",
    },
    info: { main: "#6d8196" },
    success: { main: "#15803d" },
    error: { main: "#b91c1c" },
    warning: { main: "#b45309" },
    background: {
      default: "#fffef5",
      paper: "#ffffff",
    },
    text: {
      primary: "#4a4a4a",
      secondary: "#6b7280",
      onDark: "#ffffe3",
    },
    divider: "#cbcbcb",
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    button: {
      textTransform: "none",
      fontWeight: 600,
    },
  },
});
