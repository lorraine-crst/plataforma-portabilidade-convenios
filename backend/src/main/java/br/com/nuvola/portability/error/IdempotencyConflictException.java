package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class IdempotencyConflictException extends ApiException {

	public IdempotencyConflictException() {
		super(HttpStatus.UNPROCESSABLE_CONTENT, "IDEMPOTENCIA_CONFLITO",
				"Esta Idempotency-Key já foi usada com dados diferentes.");
	}

}