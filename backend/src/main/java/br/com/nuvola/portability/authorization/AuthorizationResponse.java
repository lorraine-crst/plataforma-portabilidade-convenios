package br.com.nuvola.portability.authorization;

import java.time.Instant;

public record AuthorizationResponse(String autorizacaoId, Instant autorizadaEm) {

	public static AuthorizationResponse from(Authorization authorization) {
		return new AuthorizationResponse(authorization.getAuthorizationId(), authorization.getAuthorizedAt());
	}

}
