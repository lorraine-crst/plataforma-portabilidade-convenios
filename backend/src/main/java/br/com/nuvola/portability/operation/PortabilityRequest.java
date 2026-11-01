package br.com.nuvola.portability.operation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PortabilityRequest(
        @NotBlank String beneficiarioId,
        @NotEmpty List<@NotBlank String> parcelaIds,
        @NotNull @Valid DestinationAccountRequest contaDestino) {

    public List<String> normalizedInstallmentIds() {
        return parcelaIds.stream().distinct().sorted().toList();
    }

}
