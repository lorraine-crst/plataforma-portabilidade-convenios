package br.com.nuvola.portability.error;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@WebMvcTest(GlobalExceptionHandlerTest.ThrowingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandlerTest.ThrowingController.class)


class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest
	@CsvSource({
			"central, 503, CENTRAL_INDISPONIVEL",
			"uncertain, 504, ENVIO_INCERTO",
			"not-authorized, 422, BENEFICIARIO_NAO_AUTORIZADO",
			"not-found, 404, OPERACAO_NAO_ENCONTRADA",
			"conflict, 422, IDEMPOTENCIA_CONFLITO",
			"installment, 422, PARCELA_INVALIDA"
	})
	void mapsApiExceptionsToStatusAndCode(String type, int expectedStatus, String expectedCode) throws Exception {
		mockMvc.perform(get("/test-errors/{type}", type))
				.andExpect(status().is(expectedStatus))
				.andExpect(jsonPath("$.codigo").value(expectedCode))
				.andExpect(jsonPath("$.mensagem").isNotEmpty());
	}

	@Test
	void returnsValidationErrorForInvalidBody() throws Exception {
		mockMvc.perform(post("/test-errors/validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("VALIDACAO"))
				.andExpect(jsonPath("$.mensagem").value(containsString("name")));
	}

	@Test
	void returnsValidationErrorForMalformedJson() throws Exception {
		mockMvc.perform(post("/test-errors/validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ quebrado"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("VALIDACAO"));
	}

	@Test
	void returnsValidationErrorForMissingHeader() throws Exception {
		mockMvc.perform(get("/test-errors/header"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("VALIDACAO"))
				.andExpect(jsonPath("$.mensagem").value(containsString("Idempotency-Key")));
	}

	@Test
	void hidesInternalDetailsOnUnexpectedError() throws Exception {
		mockMvc.perform(get("/test-errors/{type}", "unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
				.andExpect(jsonPath("$.mensagem").value(not(containsString("secreto"))));
	}

	@RestController
	public static class ThrowingController {

		@GetMapping("/test-errors/{type}")
		public void throwError(@PathVariable String type) {
			throw switch (type) {
				case "central" -> new CentralUnavailableException();
				case "uncertain" -> new UncertainSubmissionException();
				case "not-authorized" -> new BeneficiaryNotAuthorizedException("BEN003");
				case "not-found" -> new OperationNotFoundException("OP-INEXISTENTE");
				case "conflict" -> new IdempotencyConflictException();
				case "installment" -> new InvalidInstallmentException("Parcela P999 não existe na agenda.");
				default -> new IllegalStateException("detalhe interno secreto");
			};
		}

		@PostMapping("/test-errors/validation")
		public void validate(@Valid @RequestBody SampleRequest request) {
		}

		@GetMapping("/test-errors/header")
		public void requireHeader(@RequestHeader("Idempotency-Key") String idempotencyKey) {
		}

	}

	public record SampleRequest(@NotBlank String name) {
	}

}
