package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class AuthorizationNotFoundException extends ApiException {

	public AuthorizationNotFoundException() {
		super(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Você ainda não registrou a autorização.");
	}

}
