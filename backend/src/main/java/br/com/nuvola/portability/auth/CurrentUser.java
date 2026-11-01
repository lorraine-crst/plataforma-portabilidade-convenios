package br.com.nuvola.portability.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

	public String username() {
		return jwt().getSubject();
	}

	public Role role() {
		return Role.valueOf(jwt().getClaimAsString(TokenService.ROLE_CLAIM));
	}

	public boolean isOperator() {
		return role() == Role.OPERADOR;
	}

	private Jwt jwt() {
		return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

}
