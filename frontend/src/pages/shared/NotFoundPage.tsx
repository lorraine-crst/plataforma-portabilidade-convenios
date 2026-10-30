import { Container } from "@mui/material";
import { ErrorPage } from "../../components/ErrorPage";

export function NotFoundPage() {
  return (
    <Container maxWidth="sm">
      <ErrorPage
        title="Página não encontrada"
        message="O endereço que você acessou não existe."
      />
    </Container>
  );
}
