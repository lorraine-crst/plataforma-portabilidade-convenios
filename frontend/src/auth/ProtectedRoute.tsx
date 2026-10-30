import { Navigate, Outlet } from "react-router";
import type { Role } from "../types";
import { useAuth } from "./AuthContext";
import { getHomePath } from "./roles";

interface ProtectedRouteProps {
  role?: Role;
}

export function ProtectedRoute({ role }: ProtectedRouteProps) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (role && user.perfil !== role) {
    return <Navigate to={getHomePath(user.perfil)} replace />;
  }

  return <Outlet />;
}
