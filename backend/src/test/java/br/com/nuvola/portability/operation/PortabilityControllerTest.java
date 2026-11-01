package br.com.nuvola.portability.operation;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nuvola.portability.error.IdempotencyConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PortabilityController.class)
@AutoConfigureMockMvc(addFilters = false)
class PortabilityControllerTest {

    private static final String VALID_BODY = """
            { "beneficiarioId": "BEN001", "parcelaIds": ["P001"],
              "contaDestino": { "banco": "001", "agencia": "1234", "conta": "56789-0" } }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortabilityService portabilityService;

    @Test
    void returns202WithOperationIdAndPublicStatus() throws Exception {
        Operation operation = new Operation();
        operation.setOperationId("OP-1");
        operation.changeStatus(OperationStatus.ENVIANDO, "Solicitação registrada");
        operation.changeStatus(OperationStatus.PENDENTE, "Aceita pela Central");
        when(portabilityService.create(eq("KEY-1"), any())).thenReturn(operation);

        mockMvc.perform(post("/api/portabilidades")
                .header("Idempotency-Key", "KEY-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.operacaoId").value("OP-1"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void showsUncertainSubmissionAsPending() throws Exception {
        Operation operation = new Operation();
        operation.setOperationId("OP-1");
        operation.changeStatus(OperationStatus.ENVIO_INCERTO, "A Central não respondeu a tempo");
        when(portabilityService.create(eq("KEY-1"), any())).thenReturn(operation);

        mockMvc.perform(post("/api/portabilidades")
                .header("Idempotency-Key", "KEY-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void returns400WhenIdempotencyKeyIsMissing() throws Exception {
        mockMvc.perform(post("/api/portabilidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.mensagem").value(containsString("Idempotency-Key")));
    }

    @Test
    void returns400WhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/portabilidades")
                .header("Idempotency-Key", "KEY-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "beneficiarioId": "", "parcelaIds": [],
                          "contaDestino": { "banco": "", "agencia": "1234", "conta": "56789-0" } }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.mensagem").value(containsString("contaDestino.banco")));
    }

    @Test
    void returns409WhenKeyWasUsedWithDifferentData() throws Exception {
        when(portabilityService.create(eq("KEY-1"), any())).thenThrow(new IdempotencyConflictException());

        mockMvc.perform(post("/api/portabilidades")
                .header("Idempotency-Key", "KEY-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"));
    }

}
