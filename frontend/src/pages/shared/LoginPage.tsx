import {
  Button,
  Container,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router";
import { ApiError } from "../../api/ApiError";
import { useAuth } from "../../auth/AuthContext";
import { getHomePath } from "../../auth/roles";
import { AppAlert } from "../../components/AppAlert";
import {
  hasErrors,
  validateLogin,
  type FieldErrors,
} from "../../utils/validation";

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
    <Container maxWidth="xs" sx={{ py: 8 }}>
      <Paper variant="outlined" sx={{ p: 4 }}>
        <Stack spacing={3}>
          <Typography variant="h4" component="h1">
            Entrar na Nuvola
          </Typography>

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
            <Stack spacing={2}>
              <TextField
                id="login-username"
                label="Usuário"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                error={Boolean(errors.username)}
                helperText={errors.username}
                autoComplete="username"
                autoFocus
                fullWidth
              />
              <TextField
                id="login-password"
                label="Senha"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                error={Boolean(errors.password)}
                helperText={errors.password}
                autoComplete="current-password"
                fullWidth
              />
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={isSubmitting}
              >
                {isSubmitting ? "Entrando…" : "Entrar"}
              </Button>
            </Stack>
          </form>
        </Stack>
      </Paper>
    </Container>
  );
}
