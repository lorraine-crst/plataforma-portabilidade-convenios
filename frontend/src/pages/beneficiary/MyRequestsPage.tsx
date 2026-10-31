import { Button, Stack, Typography } from "@mui/material";
import { useState } from "react";
import { fetchMyAuthorization } from "../../api/endpoints";
import { AppAlert } from "../../components/AppAlert";
import { Loading } from "../../components/Loading";
import { useFetch } from "../../hooks/useFetch";
import { useNotification } from "../../notifications/NotificationContext";
import type { AuthorizationResult } from "../../types";
import { formatDateTime } from "../../utils/format";
import { AuthorizationDialog } from "./AuthorizationDialog";
import { RequestsPanel } from "../shared/RequestsPanel";

export function MyRequestsPage() {
  const { notify } = useNotification();
  const {
    data: authorization,
    isLoading,
    error,
    reload,
  } = useFetch(fetchMyAuthorization, []);

  const [authorizedNow, setAuthorizedNow] = useState(false);
  const [dialogDismissed, setDialogDismissed] = useState(false);

  const needsAuthorization =
    !isLoading && !error && authorization === null && !authorizedNow;

  function handleAuthorized(result: AuthorizationResult) {
    const authorizedAt = formatDateTime(result.autorizadaEm);

    if (result.alreadyExisted) {
      notify(`Autorização já registrada em ${authorizedAt}.`, "info");
    } else {
      notify(`Autorização registrada em ${authorizedAt}.`, "success");
    }

    setAuthorizedNow(true);
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h4" component="h1">
        Minhas solicitações
      </Typography>

      {isLoading && <Loading text="Verificando sua autorização…" />}

      {error && (
        <Stack spacing={2} sx={{ alignItems: "flex-start" }}>
          <AppAlert type="error">{error.message}</AppAlert>
          <Button variant="outlined" onClick={reload}>
            Tentar de novo
          </Button>
        </Stack>
      )}

      {needsAuthorization && dialogDismissed && (
        <Stack spacing={2} sx={{ alignItems: "flex-start" }}>
          <AppAlert type="info">
            Você ainda não autorizou a Nuvola a consultar sua agenda de
            descontos.
          </AppAlert>
          <Button variant="contained" onClick={() => setDialogDismissed(false)}>
            Autorizar agora
          </Button>
        </Stack>
      )}
      <RequestsPanel showBeneficiary={false} />

      <AuthorizationDialog
        open={needsAuthorization && !dialogDismissed}
        onClose={() => setDialogDismissed(true)}
        onAuthorized={handleAuthorized}
      />
    </Stack>
  );
}
