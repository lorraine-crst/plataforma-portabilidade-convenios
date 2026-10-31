import { Button, Paper, Stack, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { fetchOperation } from "../../api/endpoints";
import { useAuth } from "../../auth/AuthContext";
import { AppAlert } from "../../components/AppAlert";
import { ErrorPage } from "../../components/ErrorPage";
import { Loading } from "../../components/Loading";
import { StatusBadge } from "../../components/StatusBadge";
import { useFetch } from "../../hooks/useFetch";
import { formatCurrency, formatDateTime } from "../../utils/format";
import { OperationDetails } from "./OperationDetails";
import { OperationTimeline } from "./OperationTimeline";

const POLL_INTERVAL_MS = 2000;
const MAX_POLLING_MS = 60000;
const KNOWN_STATUSES: string[] = ["PENDENTE", "CONFIRMADA", "REJEITADA"];

export function OperationTrackingPage() {
  const { id = "" } = useParams();
  const { user } = useAuth();
  const [pollingStopped, setPollingStopped] = useState(false);

  const {
    data: operation,
    error,
    reload,
  } = useFetch(() => fetchOperation(id), [id]);

  const isPending = operation?.status === "PENDENTE";

  useEffect(() => {
    if (!isPending || pollingStopped) {
      return;
    }

    const interval = setInterval(reload, POLL_INTERVAL_MS);
    const timeout = setTimeout(() => setPollingStopped(true), MAX_POLLING_MS);

    return () => {
      clearInterval(interval);
      clearTimeout(timeout);
    };
  }, [isPending, pollingStopped, reload]);

  function refreshNow() {
    setPollingStopped(false);
    reload();
  }

  if (!operation) {
    if (error?.code === "NAO_ENCONTRADO") {
      return (
        <ErrorPage
          title="Solicitação não encontrada"
          message="Confira o endereço ou volte ao início."
        />
      );
    }

    if (error?.code === "ACESSO_NEGADO") {
      return (
        <ErrorPage
          title="Acesso negado"
          message="Você não tem acesso a esta solicitação."
        />
      );
    }

    if (error) {
      return (
        <Stack spacing={2} sx={{ alignItems: "flex-start" }}>
          <AppAlert type="error">{error.message}</AppAlert>
          <Button variant="outlined" onClick={reload}>
            Tentar de novo
          </Button>
        </Stack>
      );
    }

    return <Loading text="Carregando a solicitação…" />;
  }

  const isKnownStatus = KNOWN_STATUSES.includes(operation.status);
  const isOperator = user?.perfil === "OPERADOR";

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        sx={{
          justifyContent: "space-between",
          alignItems: { sm: "center" },
        }}
      >
        <div>
          <Typography variant="h4" component="h1">
            Acompanhamento
          </Typography>
          <Typography color="textSecondary">
            Solicitada em {formatDateTime(operation.criadaEm)}
          </Typography>
        </div>
        <StatusBadge status={operation.status} />
      </Stack>

      {error && (
        <AppAlert type="error">
          Não foi possível atualizar o status: {error.message}
        </AppAlert>
      )}

      <Stack spacing={2} aria-live="polite">
        {isPending && !pollingStopped && (
          <Loading text="Aguardando confirmação da Central…" />
        )}

        {isPending && pollingStopped && (
          <AppAlert type="info">
            A Central ainda não respondeu. A atualização automática foi pausada;
            use "Atualizar agora" para consultar de novo.
          </AppAlert>
        )}

        {operation.status === "CONFIRMADA" && (
          <Paper
            variant="outlined"
            sx={{ p: 3, borderColor: "success.main", borderWidth: 2 }}
          >
            <Typography variant="h6" component="h2" color="success">
              Portabilidade confirmada
            </Typography>
            <Typography>
              A Central confirmou a portabilidade de {operation.parcelas.length}{" "}
              parcela(s), no total de {formatCurrency(operation.valorTotal)}.
              Protocolo: {operation.operacaoId}.
            </Typography>
          </Paper>
        )}

        {operation.status === "REJEITADA" && (
          <Paper
            variant="outlined"
            sx={{ p: 3, borderColor: "error.main", borderWidth: 2 }}
          >
            <Typography variant="h6" component="h2" color="error">
              Portabilidade rejeitada
            </Typography>
            <Typography>
              <strong>Motivo:</strong>{" "}
              {operation.motivoRejeicao ?? "A Central não informou o motivo."}
            </Typography>
          </Paper>
        )}

        {!isKnownStatus && (
          <AppAlert type="info">
            A Central retornou um status desconhecido ({operation.status}).
            Tente atualizar em alguns instantes.
          </AppAlert>
        )}
      </Stack>

      <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
        <Button variant="outlined" onClick={refreshNow}>
          Atualizar agora
        </Button>
        <Button
          component={Link}
          to={
            isOperator
              ? "/operador/portabilidade"
              : "/beneficiario/solicitacoes"
          }
          variant="contained"
        >
          {isOperator ? "Nova portabilidade" : "Minhas solicitações"}
        </Button>
      </Stack>

      <OperationDetails operation={operation} />
      <OperationTimeline events={operation.eventos} status={operation.status} />
    </Stack>
  );
}
