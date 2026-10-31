package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class IdempotencyConflictException extends ApiException {

	public IdempotencyConflictException() {
		super(HttpStatus.CONFLICT, "CONFLITO",
				"Esta solicitação já foi enviada com outros dados.");
	}

}
