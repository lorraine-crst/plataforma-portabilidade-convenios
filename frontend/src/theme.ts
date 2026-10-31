import {
  createTheme,
  responsiveFontSizes,
  type PaletteColor,
} from "@mui/material/styles";

declare module "@mui/material/styles" {
  interface Palette {
    brown: PaletteColor;
  }

  interface PaletteOptions {
    brown?: PaletteColor;
  }

  interface TypeText {
    onDark: string;
    accent: string;
  }

  interface TypeBackground {
    cream: string;
    highlight: string;
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

export const theme = responsiveFontSizes(
  createTheme({
    palette: {
      mode: "light",
      contrastThreshold: 4.5,
      primary: {
        main: "#456a92",
        light: "#a5bcd6",
        dark: "#34506f",
        contrastText: "#ffffff",
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
        default: "#ffffff",
        paper: "#ffffff",
        cream: "#fffef5",
        highlight: "#a5bcd6",
      },
      text: {
        primary: "#231815",
        secondary: "#6b7280",
        onDark: "#ffffe3",
        accent: "#4d0e12",
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
    components: {
      MuiTableRow: {
        styleOverrides: {
          root: {
            "&.Mui-selected, &.Mui-selected:hover": {
              backgroundColor: "rgba(165, 188, 214, 0.35)",
            },
          },
        },
      },
    },
  }),
);
