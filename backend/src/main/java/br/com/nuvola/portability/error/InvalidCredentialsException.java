package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

	public InvalidCredentialsException() {
		super(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "Usuário ou senha inválidos.");
	}

}
