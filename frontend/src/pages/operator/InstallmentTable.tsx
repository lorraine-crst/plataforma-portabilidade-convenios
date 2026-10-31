import {
  Checkbox,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { AppAlert } from "../../components/AppAlert";
import { StatusBadge } from "../../components/StatusBadge";
import type { Installment } from "../../types";
import { isPastDate } from "../../utils/date";
import { formatCurrency, formatDate } from "../../utils/format";

interface InstallmentTableProps {
  installments: Installment[];
  selectedIds: string[];
  onSelectionChange: (selectedIds: string[]) => void;
}

export function InstallmentTable({
  installments,
  selectedIds,
  onSelectionChange,
}: InstallmentTableProps) {
  const selectable = installments.filter(
    (installment) => installment.status === "ABERTA",
  );
  const allSelected =
    selectable.length > 0 &&
    selectable.every((installment) =>
      selectedIds.includes(installment.parcelaId),
    );
  const someSelected = selectedIds.length > 0 && !allSelected;
  const selectedTotal = installments
    .filter((installment) => selectedIds.includes(installment.parcelaId))
    .reduce((total, installment) => total + installment.valor, 0);
  const hasPastDates = installments.some((installment) =>
    isPastDate(installment.dataDesconto),
  );

  function toggle(parcelaId: string) {
    onSelectionChange(
      selectedIds.includes(parcelaId)
        ? selectedIds.filter((id) => id !== parcelaId)
        : [...selectedIds, parcelaId],
    );
  }

  function toggleAll() {
    onSelectionChange(
      allSelected ? [] : selectable.map((installment) => installment.parcelaId),
    );
  }

  return (
    <Stack spacing={2}>
      {hasPastDates && (
        <AppAlert type="info">
          Algumas parcelas têm a data de desconto já passada e podem já ter sido
          descontadas pelo credor atual. A Central decide se elas ainda podem
          ser portadas.
        </AppAlert>
      )}

      <Paper variant="outlined">
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox
                    checked={allSelected}
                    indeterminate={someSelected}
                    disabled={selectable.length === 0}
                    onChange={toggleAll}
                    slotProps={{
                      input: { "aria-label": "Selecionar todas as parcelas" },
                    }}
                  />
                </TableCell>
                <TableCell>Parcela</TableCell>
                <TableCell>Convênio</TableCell>
                <TableCell>Data do desconto</TableCell>
                <TableCell align="right">Valor</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {installments.map((installment) => {
                const selected = selectedIds.includes(installment.parcelaId);
                const pastDate = isPastDate(installment.dataDesconto);

                return (
                  <TableRow
                    key={installment.parcelaId}
                    hover
                    selected={selected}
                  >
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={selected}
                        disabled={installment.status !== "ABERTA"}
                        onChange={() => toggle(installment.parcelaId)}
                        slotProps={{
                          input: {
                            "aria-label": `Selecionar a parcela ${installment.parcelaId}`,
                          },
                        }}
                      />
                    </TableCell>
                    <TableCell>{installment.parcelaId}</TableCell>
                    <TableCell>{installment.convenioId}</TableCell>
                    <TableCell>
                      {formatDate(installment.dataDesconto)}
                      {pastDate && (
                        <Typography
                          variant="caption"
                          color="warning"
                          sx={{ display: "block" }}
                        >
                          Data já passou
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell align="right">
                      {formatCurrency(installment.valor)}
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={installment.status} />
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Typography aria-live="polite">
        {selectedIds.length === 0
          ? "Nenhuma parcela selecionada."
          : `${selectedIds.length} parcela(s) selecionada(s) · Total: ${formatCurrency(selectedTotal)}`}
      </Typography>
    </Stack>
  );
}
