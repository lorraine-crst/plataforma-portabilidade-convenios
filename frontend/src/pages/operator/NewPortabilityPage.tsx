import { Button, Paper, Stack, Typography } from "@mui/material";
import { useState } from "react";
import { fetchSchedule } from "../../api/endpoints";
import { AppAlert } from "../../components/AppAlert";
import { EmptyState } from "../../components/EmptyState";
import { Loading } from "../../components/Loading";
import { StepIndicator } from "../../components/StepIndicator";
import { useFetch } from "../../hooks/useFetch";
import type { DestinationAccount } from "../../types";
import { hasErrors, validateDestinationAccount } from "../../utils/validation";
import { BeneficiarySearch } from "./BeneficiarySearch";
import { DestinationAccountForm } from "./DestinationAccountForm";
import { InstallmentTable } from "./InstallmentTable";

const STEPS = [
  "Consultar beneficiário",
  "Selecionar parcelas",
  "Conta de destino",
];

const EMPTY_ACCOUNT: DestinationAccount = { banco: "", agencia: "", conta: "" };

export function NewPortabilityPage() {
  const [beneficiaryId, setBeneficiaryId] = useState<string | null>(null);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [account, setAccount] = useState<DestinationAccount>(EMPTY_ACCOUNT);

  const {
    data: installments,
    isLoading,
    error,
    reload,
  } = useFetch(
    () =>
      beneficiaryId ? fetchSchedule(beneficiaryId) : Promise.resolve(null),
    [beneficiaryId],
  );

  const isSearching = beneficiaryId !== null && isLoading;
  const hasResult = beneficiaryId !== null && !isLoading && !error;
  const hasInstallments =
    hasResult && installments !== null && installments.length > 0;

  const accountErrors = validateDestinationAccount(account);
  const accountIsValid = !hasErrors(accountErrors);
  const canSubmit = selectedIds.length > 0 && accountIsValid;

  const currentStep = !hasInstallments
    ? 0
    : selectedIds.length === 0
      ? 1
      : accountIsValid
        ? STEPS.length
        : 2;

  function handleSearch(id: string) {
    setSelectedIds([]);

    if (id === beneficiaryId) {
      reload();
    } else {
      setBeneficiaryId(id);
    }
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h4" component="h1">
        Nova portabilidade
      </Typography>

      <StepIndicator steps={STEPS} currentStep={currentStep} />

      <Paper variant="outlined" sx={{ p: 3 }}>
        <BeneficiarySearch disabled={isSearching} onSearch={handleSearch} />
      </Paper>

      {isSearching && <Loading text="Consultando a agenda na Central…" />}

      {beneficiaryId &&
        !isLoading &&
        error &&
        (error.code === "BENEFICIARIO_NAO_AUTORIZADO" ? (
          <AppAlert type="info">
            O beneficiário {beneficiaryId} ainda não autorizou a Nuvola. Peça
            que ele registre a autorização antes de consultar a agenda.
          </AppAlert>
        ) : (
          <Stack spacing={2} sx={{ alignItems: "flex-start" }}>
            <AppAlert type="error">{error.message}</AppAlert>
            <Button variant="outlined" onClick={reload}>
              Tentar de novo
            </Button>
          </Stack>
        ))}

      {hasResult && installments?.length === 0 && (
        <EmptyState message="Nenhuma parcela encontrada para este beneficiário." />
      )}

      {hasInstallments && (
        <>
          <InstallmentTable
            installments={installments}
            selectedIds={selectedIds}
            onSelectionChange={setSelectedIds}
          />

          <Paper variant="outlined" sx={{ p: 3 }}>
            <Stack spacing={2}>
              <Typography variant="h6" component="h2">
                Conta de destino
              </Typography>
              <DestinationAccountForm
                account={account}
                errors={accountErrors}
                onChange={setAccount}
              />
            </Stack>
          </Paper>

          <Button
            variant="contained"
            size="large"
            disabled={!canSubmit}
            sx={{ alignSelf: "flex-start" }}
          >
            Solicitar portabilidade
          </Button>
        </>
      )}
    </Stack>
  );
}
