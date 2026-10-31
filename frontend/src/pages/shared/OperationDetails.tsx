import { Divider, Paper, Stack, Typography } from "@mui/material";
import type { Operation } from "../../types";
import { formatCurrency, formatDate } from "../../utils/format";

interface OperationDetailsProps {
  operation: Operation;
}

export function OperationDetails({ operation }: OperationDetailsProps) {
  const { contaDestino } = operation;

  return (
    <Paper variant="outlined" sx={{ p: 3 }}>
      <Stack spacing={2}>
        <Typography variant="h6" component="h2">
          Detalhes
        </Typography>

        <Typography>
          <strong>Protocolo:</strong> {operation.operacaoId}
        </Typography>
        <Typography>
          <strong>Beneficiário:</strong> {operation.beneficiarioId}
        </Typography>
        <Typography>
          <strong>Conta de destino:</strong> banco {contaDestino.banco}, agência{" "}
          {contaDestino.agencia}, conta {contaDestino.conta}
        </Typography>

        <Divider />

        <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 600 }}>
          Parcelas ({operation.parcelas.length})
        </Typography>

        <Stack spacing={1} divider={<Divider flexItem />}>
          {operation.parcelas.map((installment) => (
            <Stack
              key={installment.parcelaId}
              direction="row"
              spacing={2}
              sx={{ justifyContent: "space-between", alignItems: "center" }}
            >
              <div>
                <Typography>
                  {installment.parcelaId} · {installment.convenioId}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Desconto em {formatDate(installment.dataDesconto)}
                </Typography>
              </div>
              <Typography>{formatCurrency(installment.valor)}</Typography>
            </Stack>
          ))}
        </Stack>

        <Typography sx={{ fontWeight: 600, textAlign: "right" }}>
          Total: {formatCurrency(operation.valorTotal)}
        </Typography>
      </Stack>
    </Paper>
  );
}
