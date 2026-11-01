package br.com.nuvola.portability.central;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.nuvola.portability.config.CentralClientConfig;
import br.com.nuvola.portability.config.CentralProperties;
import br.com.nuvola.portability.error.CentralUnavailableException;
import br.com.nuvola.portability.error.UncertainSubmissionException;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(CentralClientRetryTest.TestConfig.class)
class CentralClientRetryTest {

	private static final String SCHEDULE_URL = "/beneficiarios/BEN001/agenda";
	private static final String OPERATIONS_URL = "/operacoes-portabilidade";

	@RegisterExtension
	static WireMockExtension central = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort())
			.build();

	@Autowired
	private CentralClient client;

	@Test
	void retriesUntilCentralRecovers() {
		central.stubFor(get(SCHEDULE_URL).inScenario("recovery")
				.whenScenarioStateIs(STARTED)
				.willReturn(serviceUnavailable())
				.willSetStateTo("second attempt"));
		central.stubFor(get(SCHEDULE_URL).inScenario("recovery")
				.whenScenarioStateIs("second attempt")
				.willReturn(serviceUnavailable())
				.willSetStateTo("third attempt"));
		central.stubFor(get(SCHEDULE_URL).inScenario("recovery")
				.whenScenarioStateIs("third attempt")
				.willReturn(okJson("[]")));

		List<CentralInstallment> schedule = client.getSchedule("BEN001");

		assertThat(schedule).isEmpty();
		central.verify(3, getRequestedFor(urlEqualTo(SCHEDULE_URL)));
	}

	@Test
	void givesUpAfterThreeAttemptsWhenCentralStaysUnavailable() {
		central.stubFor(get(SCHEDULE_URL).willReturn(serviceUnavailable()));

		assertThatThrownBy(() -> client.getSchedule("BEN001"))
				.isInstanceOf(CentralUnavailableException.class);
		central.verify(3, getRequestedFor(urlEqualTo(SCHEDULE_URL)));
	}

	@Test
	void retriesPostWhenCentralAnswers503() {
		central.stubFor(post(OPERATIONS_URL).willReturn(serviceUnavailable()));

		assertThatThrownBy(() -> client.createOperation(operationRequest()))
				.isInstanceOf(CentralUnavailableException.class);
		central.verify(3, postRequestedFor(urlEqualTo(OPERATIONS_URL)));
	}

	@Test
	void neverRetriesPostAfterTimeout() {
		central.stubFor(post(OPERATIONS_URL).willReturn(aResponse()
				.withStatus(202)
				.withFixedDelay(1500)));

		assertThatThrownBy(() -> client.createOperation(operationRequest()))
				.isInstanceOf(UncertainSubmissionException.class);
		central.verify(1, postRequestedFor(urlEqualTo(OPERATIONS_URL)));
	}

	private CentralOperationRequest operationRequest() {
		return new CentralOperationRequest("BEN001", List.of("P001"),
				new CentralDestinationAccount("001", "1234", "56789-0"));
	}

	@Configuration
	@Import({ CentralClientConfig.class, CentralClient.class })
	static class TestConfig {

		@Bean
		CentralProperties centralProperties() {
			return new CentralProperties(central.baseUrl(), Duration.ofSeconds(1), Duration.ofMillis(500));
		}

	}

}
