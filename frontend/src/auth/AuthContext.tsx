import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { useNavigate } from "react-router";
import { definirAoExpirarSessao, definirToken } from "../api/client";
import { login as loginNaApi } from "../api/endpoints";
import type { RespostaLogin, Usuario } from "../types";

const CHAVE_SESSAO = "nuvola.sessao";

interface ValorAuth {
  usuario: Usuario | null;
  token: string | null;
  login: (usuario: string, senha: string) => Promise<Usuario>;
  logout: () => void;
}

const AuthContext = createContext<ValorAuth | null>(null);

function lerSessaoSalva(): RespostaLogin | null {
  try {
    const salva = sessionStorage.getItem(CHAVE_SESSAO);

    if (!salva) {
      return null;
    }

    const sessao = JSON.parse(salva) as RespostaLogin;

    if (
      !sessao.token ||
      (sessao.perfil !== "BENEFICIARIO" && sessao.perfil !== "OPERADOR")
    ) {
      return null;
    }

    definirToken(sessao.token);
    return sessao;
  } catch {
    return null;
  }
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const navigate = useNavigate();
  const [sessao, setSessao] = useState<RespostaLogin | null>(lerSessaoSalva);

  const login = useCallback(async (usuario: string, senha: string) => {
    const resposta = await loginNaApi(usuario, senha);
    sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(resposta));
    definirToken(resposta.token);
    setSessao(resposta);
    return { nome: resposta.nome, perfil: resposta.perfil };
  }, []);

  const logout = useCallback(() => {
    sessionStorage.removeItem(CHAVE_SESSAO);
    definirToken(null);
    setSessao(null);
  }, []);

  useEffect(() => {
    definirAoExpirarSessao(() => {
      logout();
      navigate("/login", { replace: true, state: { sessaoExpirada: true } });
    });

    return () => {
      definirAoExpirarSessao(null);
    };
  }, [logout, navigate]);

  const valor = useMemo<ValorAuth>(
    () => ({
      usuario: sessao ? { nome: sessao.nome, perfil: sessao.perfil } : null,
      token: sessao?.token ?? null,
      login,
      logout,
    }),
    [sessao, login, logout],
  );

  return <AuthContext value={valor}>{children}</AuthContext>;
}

export function useAuth(): ValorAuth {
  const valor = useContext(AuthContext);

  if (!valor) {
    throw new Error("useAuth precisa ser usado dentro do AuthProvider");
  }

  return valor;
}
