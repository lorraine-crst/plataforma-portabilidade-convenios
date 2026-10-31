package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class InvalidInstallmentException extends ApiException {

	public InvalidInstallmentException(String message) {
		super(HttpStatus.UNPROCESSABLE_CONTENT, "PARCELA_INVALIDA", message);
	}

}
