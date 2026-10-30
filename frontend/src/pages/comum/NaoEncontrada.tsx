import { Container } from "@mui/material";
import { PaginaErro } from "../../components/PaginaErro";

export function NaoEncontrada() {
  return (
    <Container maxWidth="sm">
      <PaginaErro
        titulo="Página não encontrada"
        mensagem="O endereço que você acessou não existe."
      />
    </Container>
  );
}
