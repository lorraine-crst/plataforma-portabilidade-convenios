package br.com.nuvola.portability.operation;

import jakarta.validation.constraints.NotBlank;

public record DestinationAccountRequest(
        @NotBlank String banco,
        @NotBlank String agencia,
        @NotBlank String conta) {

    public DestinationAccount toDestinationAccount() {
        return new DestinationAccount(banco, agencia, conta);
    }

}