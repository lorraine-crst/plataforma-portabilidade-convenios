import {
  Paper,
  Stack,
  Step,
  StepLabel,
  Stepper,
  Typography,
} from "@mui/material";
import type { OperationEvent, OperationStatus } from "../../types";
import { formatTimestamp } from "../../utils/format";

interface OperationTimelineProps {
  events: OperationEvent[];
  status: OperationStatus;
}

export function OperationTimeline({ events, status }: OperationTimelineProps) {
  const isPending = status === "PENDENTE";

  return (
    <Paper variant="outlined" sx={{ p: 3 }}>
      <Stack spacing={2}>
        <Typography variant="h6" component="h2">
          Histórico
        </Typography>

        <Stepper orientation="vertical" activeStep={events.length}>
          {events.map((event) => (
            <Step key={`${event.status}-${event.ocorridoEm}`} completed>
              <StepLabel
                error={event.status === "REJEITADA"}
                optional={
                  <Typography variant="caption" color="textSecondary">
                    {formatTimestamp(event.ocorridoEm)}
                  </Typography>
                }
              >
                {event.descricao}
              </StepLabel>
            </Step>
          ))}

          {isPending && (
            <Step>
              <StepLabel
                optional={
                  <Typography variant="caption" color="textSecondary">
                    Em análise
                  </Typography>
                }
              >
                Aguardando a decisão da Central
              </StepLabel>
            </Step>
          )}
        </Stepper>
      </Stack>
    </Paper>
  );
}
