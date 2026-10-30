import { Button, Stack, Typography } from "@mui/material";
import { Link } from "react-router";

interface ErrorPageProps {
  title: string;
  message: string;
}

export function ErrorPage({ title, message }: ErrorPageProps) {
  return (
    <Stack
      spacing={2}
      sx={{ alignItems: "center", py: 8, textAlign: "center" }}
    >
      <Typography variant="h4" component="h1">
        {title}
      </Typography>
      <Typography color="text.secondary">{message}</Typography>
      <Button component={Link} to="/" variant="contained">
        Voltar ao início
      </Button>
    </Stack>
  );
}
