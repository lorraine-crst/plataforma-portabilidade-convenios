package br.com.nuvola.portability.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoConfig {

	@Bean
	public DynamoDbClient dynamoDbClient(DynamoProperties properties) {
		return DynamoDbClient.builder()
				.endpointOverride(URI.create(properties.endpoint()))
				.region(Region.of(properties.region()))
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")))
				.build();
	}

	@Bean
	public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
		return DynamoDbEnhancedClient.builder()
				.dynamoDbClient(dynamoDbClient)
				.build();
	}

}
