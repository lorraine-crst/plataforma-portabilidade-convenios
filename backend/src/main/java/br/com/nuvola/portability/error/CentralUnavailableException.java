package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class CentralUnavailableException extends ApiException {

	public CentralUnavailableException() {
		super(HttpStatus.SERVICE_UNAVAILABLE, "CENTRAL_INDISPONIVEL",
				"A Central de Convênios está indisponível no momento. Tente novamente em instantes.");
	}

}
