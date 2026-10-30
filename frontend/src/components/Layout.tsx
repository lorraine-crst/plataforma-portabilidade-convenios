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
import { nomesDosPerfis } from "../auth/perfis";
import type { Perfil } from "../types";

interface ItemMenu {
  rotulo: string;
  destino: string;
}

const menus: Record<Perfil, ItemMenu[]> = {
  BENEFICIARIO: [
    { rotulo: "Autorização", destino: "/beneficiario/autorizar" },
    { rotulo: "Minhas solicitações", destino: "/beneficiario/solicitacoes" },
  ],
  OPERADOR: [
    { rotulo: "Nova portabilidade", destino: "/operador/portabilidade" },
    { rotulo: "Painel", destino: "/operador/painel" },
  ],
};

export function Layout() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();

  function sair() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <>
      <AppBar position="static" color="secondary" elevation={0}>
        <Toolbar sx={{ gap: 2, flexWrap: "wrap", py: 1 }}>
          <Typography
            component={Link}
            to="/"
            variant="h6"
            sx={{ color: "inherit", fontWeight: 700, textDecoration: "none" }}
          >
            Nuvola
          </Typography>

          {usuario && (
            <>
              <Box
                component="nav"
                aria-label="Menu principal"
                sx={{ display: "flex", gap: 1, flexGrow: 1 }}
              >
                {menus[usuario.perfil].map((item) => (
                  <Button
                    key={item.destino}
                    component={NavLink}
                    to={item.destino}
                    color="inherit"
                    sx={{
                      "&.active": { bgcolor: "rgba(255, 255, 255, 0.15)" },
                    }}
                  >
                    {item.rotulo}
                  </Button>
                ))}
              </Box>

              <Stack direction="row" spacing={2} sx={{ alignItems: "center" }}>
                <Box sx={{ textAlign: "right" }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {usuario.nome}
                  </Typography>
                  <Typography variant="caption" sx={{ opacity: 0.85 }}>
                    {nomesDosPerfis[usuario.perfil]}
                  </Typography>
                </Box>
                <Button color="inherit" variant="outlined" onClick={sair}>
                  Sair
                </Button>
              </Stack>
            </>
          )}
        </Toolbar>
      </AppBar>
      <Container component="main" maxWidth="lg" sx={{ py: 3 }}>
        <Outlet />
      </Container>
    </>
  );
}
