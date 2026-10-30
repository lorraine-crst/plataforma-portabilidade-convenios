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
import { homeDoPerfil } from "../../auth/perfis";
import { Alerta } from "../../components/Alerta";
import { temErros, validarLogin, type Erros } from "../../utils/validacoes";

interface EstadoNavegacao {
  sessaoExpirada?: boolean;
}

export function Login() {
  const { usuario: usuarioLogado, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const estado = location.state as EstadoNavegacao | null;

  const [usuario, setUsuario] = useState("");
  const [senha, setSenha] = useState("");
  const [erros, setErros] = useState<Erros<"usuario" | "senha">>({});
  const [erroEnvio, setErroEnvio] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  if (usuarioLogado) {
    return <Navigate to={homeDoPerfil(usuarioLogado.perfil)} replace />;
  }

  async function enviar() {
    const novosErros = validarLogin(usuario, senha);
    setErros(novosErros);
    setErroEnvio(null);

    if (temErros(novosErros)) {
      return;
    }

    setEnviando(true);

    try {
      const logado = await login(usuario.trim(), senha);
      navigate(homeDoPerfil(logado.perfil), { replace: true });
    } catch (causa) {
      if (
        causa instanceof ApiError &&
        causa.codigo === "CREDENCIAIS_INVALIDAS"
      ) {
        setErroEnvio("Usuário ou senha inválidos");
      } else if (causa instanceof ApiError) {
        setErroEnvio(causa.message);
      } else {
        setErroEnvio("Ocorreu um erro inesperado. Tente novamente.");
      }

      setEnviando(false);
    }
  }

  return (
    <Container maxWidth="xs" sx={{ py: 8 }}>
      <Paper variant="outlined" sx={{ p: 4 }}>
        <Stack spacing={3}>
          <Typography variant="h4" component="h1">
            Entrar na Nuvola
          </Typography>

          {estado?.sessaoExpirada && (
            <Alerta tipo="info">Sessão expirada. Entre novamente.</Alerta>
          )}

          {erroEnvio && <Alerta tipo="erro">{erroEnvio}</Alerta>}

          <form
            noValidate
            onSubmit={(evento) => {
              evento.preventDefault();
              void enviar();
            }}
          >
            <Stack spacing={2}>
              <TextField
                id="login-usuario"
                label="Usuário"
                value={usuario}
                onChange={(evento) => setUsuario(evento.target.value)}
                error={Boolean(erros.usuario)}
                helperText={erros.usuario}
                autoComplete="username"
                autoFocus
                fullWidth
              />
              <TextField
                id="login-senha"
                label="Senha"
                type="password"
                value={senha}
                onChange={(evento) => setSenha(evento.target.value)}
                error={Boolean(erros.senha)}
                helperText={erros.senha}
                autoComplete="current-password"
                fullWidth
              />
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={enviando}
              >
                {enviando ? "Entrando…" : "Entrar"}
              </Button>
            </Stack>
          </form>
        </Stack>
      </Paper>
    </Container>
  );
}
