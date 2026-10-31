package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class OperationNotFoundException extends ApiException {

	public OperationNotFoundException(String operationId) {
		super(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO",
				"Operação " + operationId + " não encontrada.");
	}

}
