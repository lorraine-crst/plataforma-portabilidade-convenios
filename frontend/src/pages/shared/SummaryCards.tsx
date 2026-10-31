import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  Typography,
} from "@mui/material";
import type { OperationStatus, Summary } from "../../types";
import { formatCurrency } from "../../utils/format";

interface StatusCard {
  label: string;
  status: OperationStatus | null;
  getValue: (summary: Summary) => number;
}

const STATUS_CARDS: StatusCard[] = [
  { label: "Total", status: null, getValue: (summary) => summary.total },
  {
    label: "Aceitas",
    status: "CONFIRMADA",
    getValue: (summary) => summary.confirmadas,
  },
  {
    label: "Rejeitadas",
    status: "REJEITADA",
    getValue: (summary) => summary.rejeitadas,
  },
  {
    label: "Pendentes",
    status: "PENDENTE",
    getValue: (summary) => summary.pendentes,
  },
];

interface SummaryCardsProps {
  summary: Summary;
  selectedStatus: OperationStatus | null;
  onSelectStatus: (status: OperationStatus | null) => void;
}

export function SummaryCards({
  summary,
  selectedStatus,
  onSelectStatus,
}: SummaryCardsProps) {
  return (
    <Box
      sx={{
        display: "grid",
        gap: 2,
        gridTemplateColumns: { xs: "repeat(2, 1fr)", md: "repeat(5, 1fr)" },
      }}
    >
      {STATUS_CARDS.map((card) => {
        const selected = card.status === selectedStatus;

        return (
          <Card
            key={card.label}
            variant="outlined"
            sx={{
              borderColor: selected ? "primary.main" : undefined,
              borderWidth: selected ? 2 : 1,
            }}
          >
            <CardActionArea
              aria-pressed={selected}
              onClick={() => onSelectStatus(selected ? null : card.status)}
              sx={{ height: "100%" }}
            >
              <CardContent>
                <Typography variant="body2" color="textSecondary">
                  {card.label}
                </Typography>
                <Typography variant="h5" component="p" sx={{ fontWeight: 700 }}>
                  {card.getValue(summary)}
                </Typography>
              </CardContent>
            </CardActionArea>
          </Card>
        );
      })}

      <Card
        variant="outlined"
        sx={{ gridColumn: { xs: "span 2", md: "auto" } }}
      >
        <CardContent>
          <Typography variant="body2" color="textSecondary">
            Valor portado
          </Typography>
          <Typography variant="h5" component="p" sx={{ fontWeight: 700 }}>
            {formatCurrency(summary.valorTotalConfirmado)}
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
