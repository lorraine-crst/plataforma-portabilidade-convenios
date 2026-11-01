package br.com.nuvola.portability.central;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.nuvola.portability.config.CentralClientConfig;
import br.com.nuvola.portability.config.CentralProperties;
import br.com.nuvola.portability.error.CentralUnavailableException;
import br.com.nuvola.portability.error.UncertainSubmissionException;
import br.com.nuvola.portability.operation.OperationStatus;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class CentralClientTest {

	private static final String HMAC_SECRET = "test-hmac-secret";

	@RegisterExtension
	static WireMockExtension central = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort())
			.build();

	private CentralClient client;

	@BeforeEach
	void setUp() {
		client = clientFor(central.baseUrl());
	}

	@Test
	void returnsPendingWhenCentralAnswers204() {
		central.stubFor(get("/operacoes-portabilidade/PROTO-1").willReturn(noContent()));

		CentralOperationStatus result = client.getOperationStatus("PROTO-1");

		assertThat(result.status()).isEqualTo(OperationStatus.PENDENTE);
	}

	@Test
	void returnsConfirmedWhenCentralAnswers200() {
		central.stubFor(get("/operacoes-portabilidade/PROTO-1").willReturn(okJson("""
				{ "status": "CONFIRMADA", "motivoRejeicao": null }
				""")));

		CentralOperationStatus result = client.getOperationStatus("PROTO-1");

		assertThat(result.status()).isEqualTo(OperationStatus.CONFIRMADA);
		assertThat(result.rejectionReason()).isNull();
	}

	@Test
	void returnsRejectedWithReasonWhenCentralAnswers200() {
		central.stubFor(get("/operacoes-portabilidade/PROTO-1").willReturn(okJson("""
				{ "status": "REJEITADA", "motivoRejeicao": "Beneficiário atingiu o limite de margem consignável" }
				""")));

		CentralOperationStatus result = client.getOperationStatus("PROTO-1");

		assertThat(result.status()).isEqualTo(OperationStatus.REJEITADA);
		assertThat(result.rejectionReason()).isEqualTo("Beneficiário atingiu o limite de margem consignável");
	}

	@Test
	void returnsNotFoundWhenCentralAnswers404() {
		central.stubFor(get("/operacoes-portabilidade/PROTO-1").willReturn(notFound()));

		CentralOperationStatus result = client.getOperationStatus("PROTO-1");

		assertThat(result.status()).isEqualTo(OperationStatus.NAO_ENCONTRADA_NA_CENTRAL);
	}

	@Test
	void readsScheduleInstallments() {
		central.stubFor(get("/beneficiarios/BEN001/agenda").willReturn(okJson("""
				[ { "parcelaId": "P001", "convenioId": "CONV01", "valor": 150.00, "dataDesconto": "2026-01-10", "status": "ABERTA" } ]
				""")));

		List<CentralInstallment> schedule = client.getSchedule("BEN001");

		assertThat(schedule).hasSize(1);
		assertThat(schedule.getFirst().valor()).isEqualByComparingTo(new BigDecimal("150.00"));
		assertThat(schedule.getFirst().dataDesconto()).isEqualTo(LocalDate.of(2026, 1, 10));
	}

	@Test
	void returnsProtocolWhenOperationIsAccepted() {
		central.stubFor(post("/operacoes-portabilidade").willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/json")
				.withBody("""
						{ "protocolo": "PROTO-1" }
						""")));

		assertThat(client.createOperation(operationRequest())).isEqualTo("PROTO-1");
	}

	@Test
	void throwsUnavailableWhenCentralAnswers503() {
		central.stubFor(post("/operacoes-portabilidade").willReturn(serviceUnavailable()));

		assertThatThrownBy(() -> client.createOperation(operationRequest()))
				.isInstanceOf(CentralUnavailableException.class);
	}

	@Test
	void throwsUnavailableWhenCentralIsOffline() {
		CentralClient offlineClient = clientFor("http://localhost:1");

		assertThatThrownBy(() -> offlineClient.getSchedule("BEN001"))
				.isInstanceOf(CentralUnavailableException.class);
	}

	@Test
	void throwsUncertainWhenOperationResponseIsSlowerThanTimeout() {
		central.stubFor(post("/operacoes-portabilidade").willReturn(aResponse()
				.withStatus(202)
				.withFixedDelay(1500)));

		assertThatThrownBy(() -> client.createOperation(operationRequest()))
				.isInstanceOf(UncertainSubmissionException.class);
	}

	@Test
	void signsPostWithTimestampAndHmacOfExactBody() {
		central.stubFor(post("/operacoes-portabilidade").willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/json")
				.withBody("""
						{ "protocolo": "PROTO-1" }
						""")));

		client.createOperation(operationRequest());

		LoggedRequest received = central.findAll(postRequestedFor(urlEqualTo("/operacoes-portabilidade"))).getFirst();
		String timestamp = received.getHeader(HmacSigningInterceptor.TIMESTAMP_HEADER);
		String signature = received.getHeader(HmacSigningInterceptor.SIGNATURE_HEADER);

		assertThat(timestamp).isNotBlank();
		assertThat(signature).isEqualTo(new HmacSigner(HMAC_SECRET).signRequest(timestamp, received.getBody()));
	}

	@Test
	void doesNotSignGetRequests() {
		central.stubFor(get("/beneficiarios/BEN001/agenda").willReturn(okJson("[]")));

		client.getSchedule("BEN001");

		LoggedRequest received = central.findAll(getRequestedFor(urlEqualTo("/beneficiarios/BEN001/agenda"))).getFirst();
		assertThat(received.containsHeader(HmacSigningInterceptor.SIGNATURE_HEADER)).isFalse();
	}

	private CentralClient clientFor(String url) {
		CentralProperties properties = new CentralProperties(url, Duration.ofSeconds(1), Duration.ofMillis(500));
		return new CentralClient(new CentralClientConfig().centralRestClient(properties, new HmacSigner(HMAC_SECRET)));
	}

	private CentralOperationRequest operationRequest() {
		return new CentralOperationRequest("BEN001", List.of("P001"),
				new CentralDestinationAccount("001", "1234", "56789-0"));
	}

}
