import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

const raiz = document.getElementById("root");

if (!raiz) {
  throw new Error("Elemento #root não encontrado");
}

createRoot(raiz).render(
  <StrictMode>
    <h1>Olá, Nuvola</h1>
  </StrictMode>,
);
