import { Navigate } from "react-router";
import { useAuth } from "./AuthContext";
import { homeDoPerfil } from "./perfis";

export function RedirecionarInicio() {
  const { usuario } = useAuth();

  return (
    <Navigate to={usuario ? homeDoPerfil(usuario.perfil) : "/login"} replace />
  );
}
