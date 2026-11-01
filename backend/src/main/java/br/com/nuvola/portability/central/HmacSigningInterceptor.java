package br.com.nuvola.portability.central;

import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class HmacSigningInterceptor implements ClientHttpRequestInterceptor {

	public static final String TIMESTAMP_HEADER = "X-Timestamp";
	public static final String SIGNATURE_HEADER = "X-Assinatura";

	private final HmacSigner signer;

	public HmacSigningInterceptor(HmacSigner signer) {
		this.signer = signer;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		if (HttpMethod.POST.equals(request.getMethod())) {
			String timestamp = String.valueOf(Instant.now().getEpochSecond());
			request.getHeaders().set(TIMESTAMP_HEADER, timestamp);
			request.getHeaders().set(SIGNATURE_HEADER, signer.signRequest(timestamp, body));
		}
		return execution.execute(request, body);
	}

}
