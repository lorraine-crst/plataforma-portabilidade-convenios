package br.com.nuvola.portability.authorization;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nuvola.portability.auth.CurrentUser;
import br.com.nuvola.portability.error.AuthorizationNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthorizationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthorizationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthorizationService authorizationService;

	@MockitoBean
	private CurrentUser currentUser;

	@BeforeEach
	void setUp() {
		when(currentUser.username()).thenReturn("BEN001");
	}

	@Test
	void returns201WhenAuthorizationIsCreated() throws Exception {
		when(authorizationService.authorize("BEN001")).thenReturn(new AuthorizationResult(authorization(), true));

		mockMvc.perform(post("/api/autorizacao"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.autorizacaoId").value("AUT-1"))
				.andExpect(jsonPath("$.autorizadaEm").value("2026-10-29T21:40:00Z"));
	}

	@Test
	void returns200WhenAuthorizationAlreadyExisted() throws Exception {
		when(authorizationService.authorize("BEN001")).thenReturn(new AuthorizationResult(authorization(), false));

		mockMvc.perform(post("/api/autorizacao"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.autorizacaoId").value("AUT-1"));
	}

	@Test
	void returnsMyAuthorization() throws Exception {
		when(authorizationService.findMine("BEN001")).thenReturn(authorization());

		mockMvc.perform(get("/api/autorizacao"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.autorizacaoId").value("AUT-1"));
	}

	@Test
	void returns404WhenBeneficiaryHasNotAuthorized() throws Exception {
		when(authorizationService.findMine("BEN001")).thenThrow(new AuthorizationNotFoundException());

		mockMvc.perform(get("/api/autorizacao"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
	}

	private Authorization authorization() {
		return new Authorization("BEN001", "AUT-1", Instant.parse("2026-10-29T21:40:00Z"));
	}

}