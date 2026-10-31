import { Stack, TextField } from "@mui/material";
import { useState } from "react";
import type { DestinationAccount } from "../../types";
import type { FieldErrors } from "../../utils/validation";

type AccountField = keyof DestinationAccount;

const FIELDS: { name: AccountField; label: string; maxLength: number }[] = [
  { name: "banco", label: "Banco", maxLength: 3 },
  { name: "agencia", label: "Agência", maxLength: 4 },
  { name: "conta", label: "Conta", maxLength: 12 },
];

interface DestinationAccountFormProps {
  account: DestinationAccount;
  errors: FieldErrors<AccountField>;
  onChange: (account: DestinationAccount) => void;
}

export function DestinationAccountForm({
  account,
  errors,
  onChange,
}: DestinationAccountFormProps) {
  const [touched, setTouched] = useState<
    Partial<Record<AccountField, boolean>>
  >({});

  return (
    <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
      {FIELDS.map((field) => {
        const showError = touched[field.name] && Boolean(errors[field.name]);

        return (
          <TextField
            key={field.name}
            id={`account-${field.name}`}
            label={field.label}
            value={account[field.name]}
            onChange={(event) =>
              onChange({ ...account, [field.name]: event.target.value })
            }
            onBlur={() =>
              setTouched((current) => ({ ...current, [field.name]: true }))
            }
            error={showError}
            helperText={showError ? errors[field.name] : " "}
            slotProps={{
              htmlInput: {
                maxLength: field.maxLength,
                inputMode: field.name === "conta" ? "text" : "numeric",
              },
            }}
            sx={{ flex: field.name === "conta" ? 2 : 1 }}
          />
        );
      })}
    </Stack>
  );
}
