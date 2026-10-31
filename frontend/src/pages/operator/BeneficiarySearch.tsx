import { Button, Stack, TextField } from "@mui/material";
import { useState } from "react";
import { hasErrors, validateBeneficiaryId } from "../../utils/validation";

interface BeneficiarySearchProps {
  disabled: boolean;
  onSearch: (beneficiaryId: string) => void;
}

export function BeneficiarySearch({
  disabled,
  onSearch,
}: BeneficiarySearchProps) {
  const [value, setValue] = useState("");
  const [error, setError] = useState<string | undefined>();

  function submit() {
    const errors = validateBeneficiaryId(value);
    setError(errors.beneficiaryId);

    if (hasErrors(errors)) {
      return;
    }

    onSearch(value.trim().toUpperCase());
  }

  return (
    <form
      noValidate
      onSubmit={(event) => {
        event.preventDefault();
        submit();
      }}
    >
      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        sx={{ alignItems: { sm: "flex-start" } }}
      >
        <TextField
          id="beneficiary-id"
          label="ID do beneficiário"
          value={value}
          onChange={(event) => {
            setValue(event.target.value);
            setError(undefined);
          }}
          error={Boolean(error)}
          helperText={error ?? "Ex.: BEN001"}
          autoFocus
          sx={{ flexGrow: 1 }}
        />
        <Button
          type="submit"
          variant="contained"
          size="large"
          disabled={disabled}
          sx={{ py: 1.75 }}
        >
          Consultar
        </Button>
      </Stack>
    </form>
  );
}
