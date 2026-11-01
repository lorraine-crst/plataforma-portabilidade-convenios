package br.com.nuvola.portability.config;

import br.com.nuvola.portability.auth.TokenService;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
public class JwtConfig {

	@Bean
	public JwtEncoder jwtEncoder(SecretsProperties secrets) {
		return NimbusJwtEncoder.withSecretKey(secretKey(secrets))
				.algorithm(MacAlgorithm.HS256)
				.build();
	}

	@Bean
	public JwtDecoder jwtDecoder(SecretsProperties secrets) {
		return NimbusJwtDecoder.withSecretKey(secretKey(secrets))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName(TokenService.ROLE_CLAIM);
		authorities.setAuthorityPrefix("ROLE_");

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		return converter;
	}

	private SecretKey secretKey(SecretsProperties secrets) {
		return new SecretKeySpec(secrets.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

}
