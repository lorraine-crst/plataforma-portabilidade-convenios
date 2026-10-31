import { Stack, Typography } from "@mui/material";
import { RequestsPanel } from "../shared/RequestsPanel";

export function DashboardPage() {
  return (
    <Stack spacing={3}>
      <Typography variant="h4" component="h1">
        Painel de solicitações
      </Typography>
      <RequestsPanel showBeneficiary />
    </Stack>
  );
}
