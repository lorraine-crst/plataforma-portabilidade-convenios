package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class UncertainSubmissionException extends ApiException {

	public UncertainSubmissionException() {
		super(HttpStatus.GATEWAY_TIMEOUT, "ENVIO_INCERTO",
				"A Central não respondeu a tempo. A solicitação pode ter sido registrada; consulte o status antes de reenviar.");
	}

}
