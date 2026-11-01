package br.com.nuvola.portability.central;

import br.com.nuvola.portability.error.CentralUnavailableException;
import br.com.nuvola.portability.error.UncertainSubmissionException;
import br.com.nuvola.portability.operation.OperationStatus;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class CentralClient {

	private final RestClient restClient;

	public CentralClient(RestClient centralRestClient) {
		this.restClient = centralRestClient;
	}

	@Retryable(includes = CentralUnavailableException.class, maxRetries = 2, delay = 500, multiplier = 2)
	public CentralAuthorizationResponse registerAuthorization(String beneficiaryId) {
		return call(false, () -> restClient.post()
				.uri("/autorizacoes")
				.contentType(MediaType.APPLICATION_JSON)
				.body(new CentralAuthorizationRequest(beneficiaryId))
				.retrieve()
				.body(CentralAuthorizationResponse.class));
	}

	@Retryable(includes = CentralUnavailableException.class, maxRetries = 2, delay = 500, multiplier = 2)
	public List<CentralInstallment> getSchedule(String beneficiaryId) {
		return call(false, () -> restClient.get()
				.uri("/beneficiarios/{id}/agenda", beneficiaryId)
				.retrieve()
				.body(new ParameterizedTypeReference<List<CentralInstallment>>() {
				}));
	}

	@Retryable(includes = CentralUnavailableException.class, maxRetries = 2, delay = 500, multiplier = 2)
	public String createOperation(CentralOperationRequest request) {
		CentralProtocolResponse response = call(true, () -> restClient.post()
				.uri("/operacoes-portabilidade")
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(CentralProtocolResponse.class));
		return response.protocolo();
	}

	@Retryable(includes = CentralUnavailableException.class, maxRetries = 2, delay = 500, multiplier = 2)
	public CentralOperationStatus getOperationStatus(String protocol) {
		return call(false, () -> restClient.get()
				.uri("/operacoes-portabilidade/{protocolo}", protocol)
				.exchange((request, response) -> {
					int code = response.getStatusCode().value();
					if (code == 204) {
						return new CentralOperationStatus(OperationStatus.PENDENTE, null);
					}
					if (code == 404) {
						return new CentralOperationStatus(OperationStatus.NAO_ENCONTRADA_NA_CENTRAL, null);
					}
					if (response.getStatusCode().is5xxServerError()) {
						throw new CentralUnavailableException();
					}
					if (code != 200) {
						throw new IllegalStateException("Resposta inesperada da Central: " + code);
					}
					CentralOperationResult result = response.bodyTo(CentralOperationResult.class);
					return new CentralOperationStatus(OperationStatus.valueOf(result.status()), result.motivoRejeicao());
				}));
	}

	private <T> T call(boolean uncertainOnTimeout, Supplier<T> request) {
		try {
			return request.get();
		} catch (HttpServerErrorException e) {
			throw new CentralUnavailableException();
		} catch (ResourceAccessException e) {
			if (uncertainOnTimeout && isResponseTimeout(e)) {
				throw new UncertainSubmissionException();
			}
			throw new CentralUnavailableException();
		}
	}

	private boolean isResponseTimeout(Throwable error) {
		for (Throwable cause = error; cause != null; cause = cause.getCause()) {
			if (cause instanceof HttpConnectTimeoutException) {
				return false;
			}
			if (cause instanceof HttpTimeoutException) {
				return true;
			}
		}
		return false;
	}

}
