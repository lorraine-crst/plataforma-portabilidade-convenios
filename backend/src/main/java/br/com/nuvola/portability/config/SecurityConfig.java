package br.com.nuvola.portability.config;

import br.com.nuvola.portability.auth.Role;
import br.com.nuvola.portability.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SecurityConfig {

	private static final String OPERATOR = Role.OPERADOR.name();
	private static final String BENEFICIARY = Role.BENEFICIARIO.name();

	private final JsonMapper jsonMapper;

	public SecurityConfig(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter)
			throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.requestMatchers("/actuator/health", "/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/api/autorizacao").hasRole(BENEFICIARY)
						.requestMatchers(HttpMethod.GET, "/api/beneficiarios/*/agenda").hasRole(OPERATOR)
						.requestMatchers(HttpMethod.POST, "/api/portabilidades").hasRole(OPERATOR)
						.requestMatchers(HttpMethod.GET, "/api/portabilidades/**").hasAnyRole(OPERATOR, BENEFICIARY)
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
						.authenticationEntryPoint(this::sessionExpired))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(this::sessionExpired)
						.accessDeniedHandler(this::accessDenied));
		return http.build();
	}

	private void sessionExpired(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
			throws IOException {
		writeError(response, HttpStatus.UNAUTHORIZED, "SESSAO_EXPIRADA", "Sessão expirada. Faça login novamente.");
	}

	private void accessDenied(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
			throws IOException {
		writeError(response, HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Você não tem permissão para acessar este recurso.");
	}

	private void writeError(HttpServletResponse response, HttpStatus status, String code, String message)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		jsonMapper.writeValue(response.getOutputStream(), new ErrorResponse(code, message));
	}

}
