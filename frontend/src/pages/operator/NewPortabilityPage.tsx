import { Button, Paper, Stack, Typography } from "@mui/material";
import { useRef, useState } from "react";
import { useNavigate } from "react-router";
import { ApiError } from "../../api/ApiError";
import { createPortability, fetchSchedule } from "../../api/endpoints";
import { AppAlert } from "../../components/AppAlert";
import { EmptyState } from "../../components/EmptyState";
import { ErrorPage } from "../../components/ErrorPage";
import { Loading } from "../../components/Loading";
import { StepIndicator } from "../../components/StepIndicator";
import { useFetch } from "../../hooks/useFetch";
import { useNotification } from "../../notifications/NotificationContext";
import type { DestinationAccount, NewPortability } from "../../types";
import { generateId } from "../../utils/generateId";
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

interface IdempotencyEntry {
  fingerprint: string;
  key: string;
}

export function NewPortabilityPage() {
  const navigate = useNavigate();
  const { notify } = useNotification();

  const [beneficiaryId, setBeneficiaryId] = useState<string | null>(null);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [account, setAccount] = useState<DestinationAccount>(EMPTY_ACCOUNT);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<ApiError | null>(null);
  const [hasConflict, setHasConflict] = useState(false);

  const idempotencyRef = useRef<IdempotencyEntry | null>(null);
  const submittingRef = useRef(false);

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
    setSubmitError(null);

    if (id === beneficiaryId) {
      reload();
    } else {
      setBeneficiaryId(id);
    }
  }

  function handleSelectionChange(ids: string[]) {
    setSelectedIds(ids);
    setSubmitError(null);
  }

  function handleAccountChange(nextAccount: DestinationAccount) {
    setAccount(nextAccount);
    setSubmitError(null);
  }

  function getIdempotencyKey(fingerprint: string): string {
    const previous = idempotencyRef.current;

    if (previous && previous.fingerprint === fingerprint) {
      return previous.key;
    }

    const key = generateId();
    idempotencyRef.current = { fingerprint, key };
    return key;
  }

  async function submit() {
    if (submittingRef.current || !canSubmit || !beneficiaryId) {
      return;
    }

    submittingRef.current = true;
    setSubmitting(true);
    setSubmitError(null);

    const portability: NewPortability = {
      beneficiarioId: beneficiaryId,
      parcelaIds: [...selectedIds].sort(),
      contaDestino: account,
    };
    const idempotencyKey = getIdempotencyKey(JSON.stringify(portability));

    try {
      const response = await createPortability(portability, idempotencyKey);
      notify("Solicitação enviada. Acompanhe a confirmação da Central.");
      navigate(`/operacoes/${response.operacaoId}`);
    } catch (cause) {
      const apiError =
        cause instanceof ApiError
          ? cause
          : new ApiError(
              "SEM_CONEXAO",
              "Ocorreu um erro inesperado. Tente novamente.",
              0,
            );

      if (apiError.code === "CONFLITO") {
        setHasConflict(true);
      } else {
        setSubmitError(apiError);
      }
    } finally {
      submittingRef.current = false;
      setSubmitting(false);
    }
  }

  if (hasConflict) {
    return (
      <ErrorPage
        title="Solicitação em conflito"
        message="Esta solicitação já foi enviada com outros dados."
      />
    );
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h4" component="h1">
        Nova portabilidade
      </Typography>

      <StepIndicator steps={STEPS} currentStep={currentStep} />

      <Paper variant="outlined" sx={{ p: 3 }}>
        <BeneficiarySearch
          disabled={isSearching || submitting}
          onSearch={handleSearch}
        />
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
            onSelectionChange={handleSelectionChange}
          />

          <Paper variant="outlined" sx={{ p: 3 }}>
            <Stack spacing={2}>
              <Typography variant="h6" component="h2">
                Conta de destino
              </Typography>
              <DestinationAccountForm
                account={account}
                errors={accountErrors}
                onChange={handleAccountChange}
              />
            </Stack>
          </Paper>

          {submitError && (
            <AppAlert type="error">{submitError.message}</AppAlert>
          )}

          <Button
            variant="contained"
            size="large"
            disabled={!canSubmit || submitting}
            onClick={() => void submit()}
            sx={{ alignSelf: "flex-start" }}
          >
            {submitting
              ? "Enviando…"
              : submitError
                ? "Tentar novamente"
                : "Solicitar portabilidade"}
          </Button>
        </>
      )}
    </Stack>
  );
}
