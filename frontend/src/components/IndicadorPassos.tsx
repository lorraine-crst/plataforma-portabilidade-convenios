import { Step, StepLabel, Stepper } from "@mui/material";

interface IndicadorPassosProps {
  passos: string[];
  atual: number;
}

export function IndicadorPassos({ passos, atual }: IndicadorPassosProps) {
  return (
    <Stepper activeStep={atual} alternativeLabel sx={{ mb: 3 }}>
      {passos.map((passo) => (
        <Step key={passo}>
          <StepLabel>{passo}</StepLabel>
        </Step>
      ))}
    </Stepper>
  );
}
