import { Navigate, Outlet } from "react-router";
import type { Perfil } from "../types";
import { useAuth } from "./AuthContext";
import { homeDoPerfil } from "./perfis";

interface RotaProtegidaProps {
  perfil?: Perfil;
}

export function RotaProtegida({ perfil }: RotaProtegidaProps) {
  const { usuario } = useAuth();

  if (!usuario) {
    return <Navigate to="/login" replace />;
  }

  if (perfil && usuario.perfil !== perfil) {
    return <Navigate to={homeDoPerfil(usuario.perfil)} replace />;
  }

  return <Outlet />;
}
