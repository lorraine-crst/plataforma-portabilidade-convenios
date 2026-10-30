import { Route, Routes } from "react-router";
import { RedirecionarInicio } from "./auth/RedirecionarInicio";
import { RotaProtegida } from "./auth/RotaProtegida";
import { Layout } from "./components/Layout";
import { Autorizar } from "./pages/beneficiario/Autorizar";
import { Solicitacoes } from "./pages/beneficiario/Solicitacoes";
import { Acompanhamento } from "./pages/comum/Acompanhamento";
import { Login } from "./pages/comum/Login";
import { NaoEncontrada } from "./pages/comum/NaoEncontrada";
import { Painel } from "./pages/operador/Painel";
import { Portabilidade } from "./pages/operador/Portabilidade";

export function App() {
  return (
    <Routes>
      <Route path="/" element={<RedirecionarInicio />} />
      <Route path="/login" element={<Login />} />
      <Route element={<RotaProtegida />}>
        <Route element={<Layout />}>
          <Route element={<RotaProtegida perfil="BENEFICIARIO" />}>
            <Route path="/beneficiario/autorizar" element={<Autorizar />} />
            <Route
              path="/beneficiario/solicitacoes"
              element={<Solicitacoes />}
            />
          </Route>
          <Route element={<RotaProtegida perfil="OPERADOR" />}>
            <Route path="/operador/portabilidade" element={<Portabilidade />} />
            <Route path="/operador/painel" element={<Painel />} />
          </Route>
          <Route path="/operacoes/:id" element={<Acompanhamento />} />
        </Route>
      </Route>
      <Route path="*" element={<NaoEncontrada />} />
    </Routes>
  );
}
