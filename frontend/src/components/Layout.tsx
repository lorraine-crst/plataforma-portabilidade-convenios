import {
  AppBar,
  Box,
  Button,
  Container,
  Stack,
  Toolbar,
  Typography,
} from "@mui/material";
import { Link, NavLink, Outlet, useNavigate } from "react-router";
import { useAuth } from "../auth/AuthContext";
import { roleLabels } from "../auth/roles";
import type { Role } from "../types";
import { NuvolaLogo } from "./NuvolaLogo";

interface MenuItem {
  label: string;
  path: string;
}

const menus: Record<Role, MenuItem[]> = {
  BENEFICIARIO: [
    { label: "Minhas solicitações", path: "/beneficiario/solicitacoes" },
  ],
  OPERADOR: [
    { label: "Nova portabilidade", path: "/operador/portabilidade" },
    { label: "Painel", path: "/operador/painel" },
  ],
};

export function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <>
      <Box
        component="a"
        href="#conteudo"
        sx={{
          position: "absolute",
          left: 8,
          top: -48,
          zIndex: 1300,
          px: 2,
          py: 1,
          borderRadius: 1,
          bgcolor: "background.paper",
          color: "primary.main",
          fontWeight: 600,
          "&:focus": { top: 8 },
        }}
      >
        Pular para o conteúdo
      </Box>

      <AppBar position="static" color="primary" elevation={0}>
        <Toolbar sx={{ gap: 2, py: 1 }}>
          <Box
            component={Link}
            to="/"
            sx={{ color: "inherit", textDecoration: "none" }}
          >
            <NuvolaLogo size={28} />
          </Box>

          {user && (
            <>
              <Box
                component="nav"
                aria-label="Menu principal"
                sx={{ display: "flex", gap: 1, flexGrow: 1 }}
              >
                {menus[user.perfil].map((item) => (
                  <Button
                    key={item.path}
                    component={NavLink}
                    to={item.path}
                    color="inherit"
                    sx={{
                      "&.active": { bgcolor: "rgba(255, 255, 255, 0.15)" },
                    }}
                  >
                    {item.label}
                  </Button>
                ))}
              </Box>

              <Stack direction="row" spacing={2} sx={{ alignItems: "center" }}>
                <Box
                  sx={{
                    textAlign: "right",
                    display: { xs: "none", sm: "block" },
                  }}
                >
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {user.nome}
                  </Typography>
                  <Typography variant="caption" sx={{ opacity: 0.85 }}>
                    {roleLabels[user.perfil]}
                  </Typography>
                </Box>
                <Button
                  color="inherit"
                  variant="outlined"
                  onClick={handleLogout}
                >
                  Sair
                </Button>
              </Stack>
            </>
          )}
        </Toolbar>
      </AppBar>

      <Container
        component="main"
        id="conteudo"
        tabIndex={-1}
        maxWidth="lg"
        sx={{ py: 3, outline: "none" }}
      >
        <Outlet />
      </Container>
    </>
  );
}
