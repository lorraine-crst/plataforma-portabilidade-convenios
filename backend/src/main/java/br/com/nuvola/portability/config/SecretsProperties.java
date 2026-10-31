package br.com.nuvola.portability.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.secrets")
public record SecretsProperties(
		@NotBlank(message = "JWT_SECRET não definido. Copie o .env.example para .env e preencha o valor")
		@Size(min = 32, message = "JWT_SECRET precisa ter pelo menos 32 caracteres para o HS256")
		String jwtSecret,
		@NotBlank(message = "HMAC_SECRET não definido. Copie o .env.example para .env e preencha o valor")
		String hmacSecret) {
}
