package br.com.nuvola.portability.operation;

import java.time.Instant;

public record EventResponse(OperationStatus status, Instant ocorridoEm, String descricao) {

    public static EventResponse from(Event event) {
        return new EventResponse(event.getStatus().publicStatus(), event.getOccurredAt(), event.getDescription());
    }

}