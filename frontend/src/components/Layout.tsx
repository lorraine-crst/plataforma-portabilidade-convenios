import LogoutIcon from "@mui/icons-material/Logout";
import MenuIcon from "@mui/icons-material/Menu";
import {
  AppBar,
  Box,
  Button,
  Container,
  Divider,
  IconButton,
  ListItemIcon,
  Menu,
  MenuItem,
  Stack,
  Toolbar,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router";
import { useAuth } from "../auth/AuthContext";
import { roleLabels } from "../auth/roles";
import type { Role } from "../types";
import { NuvolaLogo } from "./NuvolaLogo";

interface MenuLink {
  label: string;
  path: string;
}

const menus: Record<Role, MenuLink[]> = {
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
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);

  function closeMenu() {
    setMenuAnchor(null);
  }

  function handleLogout() {
    closeMenu();
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
        <Toolbar sx={{ gap: { xs: 1, sm: 2 }, py: 1, px: { xs: 1.5, sm: 3 } }}>
          <Box
            component={Link}
            to="/"
            aria-label="Nuvola, página inicial"
            sx={{ color: "inherit", textDecoration: "none", flexShrink: 0 }}
          >
            <NuvolaLogo
              size={28}
              hideNameOnMobile={user?.perfil === "OPERADOR"}
            />
          </Box>

          {user && (
            <>
              <Box
                component="nav"
                aria-label="Menu principal"
                sx={{
                  display: "flex",
                  gap: { xs: 0.5, sm: 1 },
                  flexGrow: 1,
                  minWidth: 0,
                }}
              >
                {menus[user.perfil].map((item) => (
                  <Button
                    key={item.path}
                    component={NavLink}
                    to={item.path}
                    color="inherit"
                    sx={{
                      px: { xs: 1, sm: 2 },
                      fontSize: { xs: "0.8125rem", sm: "0.875rem" },
                      lineHeight: 1.2,
                      textAlign: "center",
                      "&.active": { bgcolor: "rgba(255, 255, 255, 0.15)" },
                    }}
                  >
                    {item.label}
                  </Button>
                ))}
              </Box>

              <Stack
                direction="row"
                spacing={2}
                sx={{
                  display: { xs: "none", md: "flex" },
                  alignItems: "center",
                  flexShrink: 0,
                }}
              >
                <Box sx={{ textAlign: "right" }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {user.nome}
                  </Typography>
                  <Typography variant="caption" sx={{ opacity: 0.85 }}>
                    {roleLabels[user.perfil]}
                  </Typography>
                </Box>
                <Button color="inherit" variant="outlined" onClick={handleLogout}>
                  Sair
                </Button>
              </Stack>

              <Box sx={{ display: { xs: "flex", md: "none" }, flexShrink: 0 }}>
                <IconButton
                  color="inherit"
                  aria-label="Abrir menu do usuário"
                  aria-controls={menuAnchor ? "menu-usuario" : undefined}
                  aria-haspopup="true"
                  aria-expanded={menuAnchor ? "true" : undefined}
                  onClick={(event) => setMenuAnchor(event.currentTarget)}
                >
                  <MenuIcon />
                </IconButton>
                <Menu
                  id="menu-usuario"
                  anchorEl={menuAnchor}
                  open={Boolean(menuAnchor)}
                  onClose={closeMenu}
                  anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
                  transformOrigin={{ vertical: "top", horizontal: "right" }}
                >
                  <Box sx={{ px: 2, py: 1 }}>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                      {user.nome}
                    </Typography>
                    <Typography variant="caption" color="textSecondary">
                      {roleLabels[user.perfil]}
                    </Typography>
                  </Box>
                  <Divider />
                  <MenuItem onClick={handleLogout}>
                    <ListItemIcon>
                      <LogoutIcon fontSize="small" />
                    </ListItemIcon>
                    Sair
                  </MenuItem>
                </Menu>
              </Box>
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
