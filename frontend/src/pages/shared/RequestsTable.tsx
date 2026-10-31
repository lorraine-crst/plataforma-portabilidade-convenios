import {
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import { Link, useNavigate } from "react-router";
import { StatusBadge } from "../../components/StatusBadge";
import type { OperationListItem } from "../../types";
import { formatCurrency, formatDateTime } from "../../utils/format";

interface RequestsTableProps {
  operations: OperationListItem[];
  showBeneficiary: boolean;
}

export function RequestsTable({
  operations,
  showBeneficiary,
}: RequestsTableProps) {
  const navigate = useNavigate();

  return (
    <Paper variant="outlined">
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Data</TableCell>
              {showBeneficiary && <TableCell>Beneficiário</TableCell>}
              <TableCell align="right">Parcelas</TableCell>
              <TableCell align="right">Valor</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Motivo</TableCell>
              <TableCell />
            </TableRow>
          </TableHead>
          <TableBody>
            {operations.map((operation) => {
              const path = `/operacoes/${operation.operacaoId}`;

              return (
                <TableRow
                  key={operation.operacaoId}
                  hover
                  onClick={() => navigate(path)}
                  sx={{ cursor: "pointer" }}
                >
                  <TableCell>{formatDateTime(operation.criadaEm)}</TableCell>
                  {showBeneficiary && (
                    <TableCell>{operation.beneficiarioId}</TableCell>
                  )}
                  <TableCell align="right">
                    {operation.quantidadeParcelas}
                  </TableCell>
                  <TableCell align="right">
                    {formatCurrency(operation.valorTotal)}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={operation.status} />
                  </TableCell>
                  <TableCell>{operation.motivoRejeicao ?? "—"}</TableCell>
                  <TableCell align="right">
                    <Button
                      component={Link}
                      to={path}
                      size="small"
                      onClick={(event) => event.stopPropagation()}
                    >
                      Detalhes
                    </Button>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}
