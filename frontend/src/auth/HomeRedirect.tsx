import { Navigate } from "react-router";
import { useAuth } from "./AuthContext";
import { getHomePath } from "./roles";

export function HomeRedirect() {
  const { user } = useAuth();

  return <Navigate to={user ? getHomePath(user.perfil) : "/login"} replace />;
}
