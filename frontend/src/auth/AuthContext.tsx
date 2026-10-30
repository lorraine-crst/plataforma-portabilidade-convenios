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
import { setAuthToken, setOnSessionExpired } from "../api/client";
import { login as loginRequest } from "../api/endpoints";
import type { LoginResponse, User } from "../types";

const SESSION_STORAGE_KEY = "nuvola.session";

interface AuthContextValue {
  user: User | null;
  token: string | null;
  login: (username: string, password: string) => Promise<User>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function restoreStoredSession(): LoginResponse | null {
  try {
    const stored = sessionStorage.getItem(SESSION_STORAGE_KEY);

    if (!stored) {
      return null;
    }

    const session = JSON.parse(stored) as LoginResponse;

    if (
      !session.token ||
      (session.perfil !== "BENEFICIARIO" && session.perfil !== "OPERADOR")
    ) {
      return null;
    }

    setAuthToken(session.token);
    return session;
  } catch {
    return null;
  }
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const navigate = useNavigate();
  const [session, setSession] = useState<LoginResponse | null>(
    restoreStoredSession,
  );

  const login = useCallback(async (username: string, password: string) => {
    const response = await loginRequest(username, password);
    sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(response));
    setAuthToken(response.token);
    setSession(response);
    return { nome: response.nome, perfil: response.perfil };
  }, []);

  const logout = useCallback(() => {
    sessionStorage.removeItem(SESSION_STORAGE_KEY);
    setAuthToken(null);
    setSession(null);
  }, []);

  useEffect(() => {
    setOnSessionExpired(() => {
      logout();
      navigate("/login", { replace: true, state: { sessionExpired: true } });
    });

    return () => {
      setOnSessionExpired(null);
    };
  }, [logout, navigate]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user: session ? { nome: session.nome, perfil: session.perfil } : null,
      token: session?.token ?? null,
      login,
      logout,
    }),
    [session, login, logout],
  );

  return <AuthContext value={value}>{children}</AuthContext>;
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext);

  if (!value) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return value;
}
