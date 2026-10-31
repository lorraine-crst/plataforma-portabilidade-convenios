import { Box, Button, LinearProgress, Stack, TextField } from "@mui/material";
import { useEffect, useState } from "react";
import { fetchSummary, listOperations } from "../../api/endpoints";
import { AppAlert } from "../../components/AppAlert";
import { EmptyState } from "../../components/EmptyState";
import { Loading } from "../../components/Loading";
import { useFetch } from "../../hooks/useFetch";
import type { OperationStatus } from "../../types";
import { RequestsTable } from "./RequestsTable";
import { SummaryCards } from "./SummaryCards";

const SEARCH_DELAY_MS = 400;

interface RequestsPanelProps {
  showBeneficiary: boolean;
}

export function RequestsPanel({ showBeneficiary }: RequestsPanelProps) {
  const [status, setStatus] = useState<OperationStatus | null>(null);
  const [beneficiaryInput, setBeneficiaryInput] = useState("");
  const [beneficiaryFilter, setBeneficiaryFilter] = useState("");

  useEffect(() => {
    const timeout = setTimeout(() => {
      setBeneficiaryFilter(beneficiaryInput.trim().toUpperCase());
    }, SEARCH_DELAY_MS);

    return () => {
      clearTimeout(timeout);
    };
  }, [beneficiaryInput]);

  const summary = useFetch(fetchSummary, []);
  const list = useFetch(
    () =>
      listOperations({
        status: status ?? undefined,
        beneficiarioId: beneficiaryFilter || undefined,
      }),
    [status, beneficiaryFilter],
  );

  const hasFilters = status !== null || beneficiaryFilter !== "";

  function refresh() {
    summary.reload();
    list.reload();
  }

  function clearFilters() {
    setStatus(null);
    setBeneficiaryInput("");
    setBeneficiaryFilter("");
  }

  return (
    <Stack spacing={3}>
      {summary.error ? (
        <AppAlert type="error">
          Não foi possível carregar o resumo: {summary.error.message}
        </AppAlert>
      ) : summary.data ? (
        <SummaryCards
          summary={summary.data}
          selectedStatus={status}
          onSelectStatus={setStatus}
        />
      ) : (
        <Loading text="Carregando o resumo…" />
      )}

      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        sx={{ alignItems: { sm: "center" } }}
      >
        {showBeneficiary && (
          <TextField
            id="filter-beneficiary"
            label="Buscar por beneficiário"
            value={beneficiaryInput}
            onChange={(event) => setBeneficiaryInput(event.target.value)}
            size="small"
            sx={{ minWidth: 240 }}
          />
        )}
        <Box sx={{ flexGrow: 1 }} />
        {hasFilters && <Button onClick={clearFilters}>Limpar filtros</Button>}
        <Button variant="outlined" onClick={refresh}>
          Atualizar
        </Button>
      </Stack>

      {list.error ? (
        <Stack spacing={2} sx={{ alignItems: "flex-start" }}>
          <AppAlert type="error">{list.error.message}</AppAlert>
          <Button variant="outlined" onClick={list.reload}>
            Tentar de novo
          </Button>
        </Stack>
      ) : !list.data ? (
        <Loading text="Carregando as solicitações…" />
      ) : list.data.length === 0 ? (
        <EmptyState
          message={
            hasFilters
              ? "Nenhuma solicitação encontrada com esses filtros."
              : "Nenhuma solicitação ainda."
          }
        />
      ) : (
        <Box>
          {list.isLoading && (
            <LinearProgress aria-label="Atualizando a lista" />
          )}
          <RequestsTable
            operations={list.data}
            showBeneficiary={showBeneficiary}
          />
        </Box>
      )}
    </Stack>
  );
}
