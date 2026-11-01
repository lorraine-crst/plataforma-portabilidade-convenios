package br.com.nuvola.portability.config;

import br.com.nuvola.portability.central.HmacSigner;
import br.com.nuvola.portability.central.HmacSigningInterceptor;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.web.client.RestClient;

@Configuration
@EnableResilientMethods
public class CentralClientConfig {

	@Bean
	public HmacSigner hmacSigner(SecretsProperties secrets) {
		return new HmacSigner(secrets.hmacSecret());
	}

	@Bean
	public RestClient centralRestClient(CentralProperties properties, HmacSigner hmacSigner) {
		HttpClient httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(properties.connectTimeout())
				.build();

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.readTimeout());

		return RestClient.builder()
				.baseUrl(properties.url())
				.requestFactory(requestFactory)
				.requestInterceptor(new HmacSigningInterceptor(hmacSigner))
				.build();
	}

}
