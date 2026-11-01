package br.com.nuvola.portability.operation;

public enum OperationStatus {
    ENVIANDO,
    PENDENTE,
    CONFIRMADA,
    REJEITADA,
    FALHA_ENVIO,
    ENVIO_INCERTO,
    NAO_ENCONTRADA_NA_CENTRAL;

    public OperationStatus publicStatus() {
        return switch (this) {
            case CONFIRMADA, REJEITADA -> this;
            default -> PENDENTE;
        };
    }

}
