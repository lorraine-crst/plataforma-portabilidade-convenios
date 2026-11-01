package br.com.nuvola.portability.operation;

public record CreatePortabilityResponse(String operacaoId, OperationStatus status) {

    public static CreatePortabilityResponse from(Operation operation) {
        return new CreatePortabilityResponse(operation.getOperationId(), operation.getStatus().publicStatus());
    }

}