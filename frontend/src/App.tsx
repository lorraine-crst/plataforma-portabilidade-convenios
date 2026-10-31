import { Route, Routes } from "react-router";
import { HomeRedirect } from "./auth/HomeRedirect";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { Layout } from "./components/Layout";
import { MyRequestsPage } from "./pages/beneficiary/MyRequestsPage";
import { DashboardPage } from "./pages/operator/DashboardPage";
import { NewPortabilityPage } from "./pages/operator/NewPortabilityPage";
import { LoginPage } from "./pages/shared/LoginPage";
import { NotFoundPage } from "./pages/shared/NotFoundPage";
import { OperationTrackingPage } from "./pages/shared/OperationTrackingPage";

export function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route element={<ProtectedRoute role="BENEFICIARIO" />}>
            <Route
              path="/beneficiario/solicitacoes"
              element={<MyRequestsPage />}
            />
          </Route>
          <Route element={<ProtectedRoute role="OPERADOR" />}>
            <Route
              path="/operador/portabilidade"
              element={<NewPortabilityPage />}
            />
            <Route path="/operador/painel" element={<DashboardPage />} />
          </Route>
          <Route path="/operacoes/:id" element={<OperationTrackingPage />} />
        </Route>
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
