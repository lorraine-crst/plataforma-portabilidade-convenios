package br.com.nuvola.portability.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.central")
public record CentralProperties(
		@NotBlank(message = "CENTRAL_URL não pode ser vazio") String url,
		@NotNull Duration connectTimeout,
		@NotNull Duration readTimeout) {
}
