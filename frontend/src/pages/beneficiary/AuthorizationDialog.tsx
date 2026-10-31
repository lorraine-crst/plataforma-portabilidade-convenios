import {
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  FormControl,
  FormControlLabel,
  FormHelperText,
  Stack,
} from "@mui/material";
import { useState } from "react";
import { ApiError } from "../../api/ApiError";
import { registerAuthorization } from "../../api/endpoints";
import { useAuth } from "../../auth/AuthContext";
import { AppAlert } from "../../components/AppAlert";
import type { AuthorizationResult } from "../../types";
import { hasErrors, validateConsent } from "../../utils/validation";

interface AuthorizationDialogProps {
  open: boolean;
  onClose: () => void;
  onAuthorized: (result: AuthorizationResult) => void;
}

export function AuthorizationDialog({
  open,
  onClose,
  onAuthorized,
}: AuthorizationDialogProps) {
  const { user } = useAuth();

  const [accepted, setAccepted] = useState(false);
  const [consentError, setConsentError] = useState<string | undefined>();
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  async function submit() {
    const errors = validateConsent(accepted);
    setConsentError(errors.consent);
    setErrorMessage(null);

    if (hasErrors(errors)) {
      return;
    }

    setSubmitting(true);

    try {
      onAuthorized(await registerAuthorization());
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Ocorreu um erro inesperado. Tente novamente.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Dialog
      open={open}
      onClose={submitting ? undefined : onClose}
      aria-labelledby="authorization-dialog-title"
      fullWidth
      maxWidth="sm"
    >
      <form
        noValidate
        onSubmit={(event) => {
          event.preventDefault();
          void submit();
        }}
      >
        <DialogTitle id="authorization-dialog-title">
          Autorizar a Nuvola
        </DialogTitle>

        <DialogContent>
          <Stack spacing={2}>
            {errorMessage && <AppAlert type="error">{errorMessage}</AppAlert>}

            <DialogContentText>
              Eu, <strong>{user?.nome}</strong>, autorizo a Nuvola a consultar
              minha agenda de descontos em folha na Central de Convênios e a
              solicitar a portabilidade das parcelas que eu indicar.
            </DialogContentText>

            <FormControl error={Boolean(consentError)}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={accepted}
                    onChange={(event) => {
                      setAccepted(event.target.checked);
                      setConsentError(undefined);
                    }}
                  />
                }
                label="Li e autorizo"
              />
              {consentError && <FormHelperText>{consentError}</FormHelperText>}
            </FormControl>
          </Stack>
        </DialogContent>

        <DialogActions>
          <Button onClick={onClose} disabled={submitting}>
            Agora não
          </Button>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? "Registrando…" : "Autorizar"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
