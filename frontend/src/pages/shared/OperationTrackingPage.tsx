import { Typography } from "@mui/material";
import { useParams } from "react-router";

export function OperationTrackingPage() {
  const { id } = useParams();

  return (
    <Typography variant="h4" component="h1">
      Solicitação {id}
    </Typography>
  );
}
