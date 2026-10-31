import Visibility from "@mui/icons-material/Visibility";
import VisibilityOff from "@mui/icons-material/VisibilityOff";
import {
  Box,
  Button,
  IconButton,
  InputAdornment,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router";
import { ApiError } from "../../api/ApiError";
import cloudsImage from "../../assets/nuvola-clouds.webp";
import { useAuth } from "../../auth/AuthContext";
import { getHomePath } from "../../auth/roles";
import { AppAlert } from "../../components/AppAlert";
import { NuvolaLogo } from "../../components/NuvolaLogo";
import {
  hasErrors,
  validateLogin,
  type FieldErrors,
} from "../../utils/validation";

const HEADER_OVERLAY = "rgba(18, 27, 41, 0.55)";
const PANEL_OVERLAY = "rgba(18, 27, 41, 0.35)";
const TEXT_SHADOW = "0 1px 4px rgba(0, 0, 0, 0.6)";

const fieldSx = {
  "& .MuiOutlinedInput-root": {
    bgcolor: "background.paper",
    borderRadius: "10px",
  },
};

interface LocationState {
  sessionExpired?: boolean;
}

export function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as LocationState | null;

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<FieldErrors<"username" | "password">>(
    {},
  );
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (user) {
    return <Navigate to={getHomePath(user.perfil)} replace />;
  }

  async function handleSubmit() {
    const validationErrors = validateLogin(username, password);
    setErrors(validationErrors);
    setSubmitError(null);

    if (hasErrors(validationErrors)) {
      return;
    }

    setIsSubmitting(true);

    try {
      const loggedUser = await login(username.trim(), password);
      navigate(getHomePath(loggedUser.perfil), { replace: true });
    } catch (cause) {
      if (cause instanceof ApiError && cause.code === "CREDENCIAIS_INVALIDAS") {
        setSubmitError("Usuário ou senha inválidos");
      } else if (cause instanceof ApiError) {
        setSubmitError(cause.message);
      } else {
        setSubmitError("Ocorreu um erro inesperado. Tente novamente.");
      }

      setIsSubmitting(false);
    }
  }

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "grid",
        gridTemplateColumns: { xs: "1fr", md: "auto minmax(0, 1fr)" },
        gridTemplateRows: { xs: "auto 1fr", md: "auto" },
        bgcolor: "background.cream",
      }}
    >
      <Box
        component="header"
        sx={{
          display: { xs: "flex", md: "none" },
          position: "relative",
          alignItems: "center",
          minHeight: 120,
          px: 3,
          py: 2,
          color: "text.onDark",
          bgcolor: "secondary.main",
          backgroundImage: `url(${cloudsImage})`,
          backgroundSize: "cover",
          backgroundPosition: "left 30%",
          backgroundRepeat: "no-repeat",
        }}
      >
        <Box sx={{ position: "absolute", inset: 0, bgcolor: HEADER_OVERLAY }} />
        <Box sx={{ position: "relative" }}>
          <NuvolaLogo size={32} showTagline />
        </Box>
      </Box>

      <Box
        sx={{
          display: { xs: "none", md: "flex" },
          position: "relative",
          alignItems: "flex-end",
          width: "min(calc(100vh * 732 / 1040), 55vw)",
          p: 6,
          color: "text.onDark",
          bgcolor: "secondary.main",
          backgroundImage: `url(${cloudsImage})`,
          backgroundSize: "cover",
          backgroundPosition: "left center",
          backgroundRepeat: "no-repeat",
        }}
      >
        <Box sx={{ position: "absolute", inset: 0, bgcolor: PANEL_OVERLAY }} />
        <Stack
          spacing={2}
          sx={{ position: "relative", maxWidth: 440, textShadow: TEXT_SHADOW }}
        >
          <NuvolaLogo size={48} showTagline />
          <Typography variant="h5" component="p" sx={{ fontWeight: 500 }}>
            Da autorização do beneficiário à confirmação da portabilidade, em um
            só lugar.
          </Typography>
        </Stack>
      </Box>

      <Box
        component="main"
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          p: { xs: 3, md: 6 },
        }}
      >
        <Stack spacing={3} sx={{ width: "100%", maxWidth: 400 }}>
          <div>
            <Typography
              variant="h4"
              component="h1"
              color="brown"
              sx={{ fontWeight: 700 }}
            >
              Entrar
            </Typography>
            <Typography sx={{ color: "brown" }}>
              Use seu usuário e senha para acessar a plataforma.
            </Typography>
          </div>

          {locationState?.sessionExpired && (
            <AppAlert type="info">Sessão expirada. Entre novamente.</AppAlert>
          )}

          {submitError && <AppAlert type="error">{submitError}</AppAlert>}

          <form
            noValidate
            onSubmit={(event) => {
              event.preventDefault();
              void handleSubmit();
            }}
          >
            <Stack spacing={2.5}>
              <TextField
                id="login-username"
                label="Usuário"
                required
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                error={Boolean(errors.username)}
                helperText={errors.username}
                autoComplete="username"
                autoFocus
                color="brown"
                fullWidth
                sx={fieldSx}
              />
              <TextField
                id="login-password"
                label="Senha"
                required
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                error={Boolean(errors.password)}
                helperText={errors.password}
                autoComplete="current-password"
                color="brown"
                fullWidth
                sx={fieldSx}
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          edge="end"
                          sx={{ color: "text.primary" }}
                          aria-label={
                            showPassword ? "Ocultar senha" : "Mostrar senha"
                          }
                          onClick={() => setShowPassword((current) => !current)}
                        >
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  },
                }}
              />
              <Button
                type="submit"
                variant="contained"
                size="large"
                color="brown"
                disabled={isSubmitting}
                sx={{ borderRadius: "10px", py: 1.5 }}
              >
                {isSubmitting ? "Entrando…" : "Entrar"}
              </Button>
            </Stack>
          </form>
        </Stack>
      </Box>
    </Box>
  );
}
