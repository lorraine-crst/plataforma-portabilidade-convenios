import { Button, Stack, Typography } from "@mui/material";
import { Link } from "react-router";

interface PaginaErroProps {
  titulo: string;
  mensagem: string;
}

export function PaginaErro({ titulo, mensagem }: PaginaErroProps) {
  return (
    <Stack
      spacing={2}
      sx={{ alignItems: "center", py: 8, textAlign: "center" }}
    >
      <Typography variant="h4" component="h1">
        {titulo}
      </Typography>
      <Typography color="text.secondary">{mensagem}</Typography>
      <Button component={Link} to="/" variant="contained">
        Voltar ao início
      </Button>
    </Stack>
  );
}
