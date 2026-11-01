package br.com.nuvola.portability.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.dynamo")
public record DynamoProperties(
		@NotBlank(message = "DYNAMO_ENDPOINT não pode ser vazio") String endpoint,
		@NotBlank String region,
		boolean createTables) {
}
