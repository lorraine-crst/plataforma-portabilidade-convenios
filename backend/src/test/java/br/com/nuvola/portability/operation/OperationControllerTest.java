package br.com.nuvola.portability.operation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nuvola.portability.auth.CurrentUser;
import br.com.nuvola.portability.central.CentralClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OperationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(OperationTrackingService.class)
class OperationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperationRepository operationRepository;

    @MockitoBean
    private CentralClient centralClient;

    @MockitoBean
    private CurrentUser currentUser;

    @Test
    void forbidsBen001FromOpeningBen002Operation() throws Exception {
        when(currentUser.isOperator()).thenReturn(false);
        when(currentUser.username()).thenReturn("BEN001");
        when(operationRepository.findById("OP-2")).thenReturn(Optional.of(rejectedOperation("OP-2", "BEN002")));

        mockMvc.perform(get("/api/portabilidades/OP-2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void returnsFullOperationInContractFormat() throws Exception {
        when(currentUser.isOperator()).thenReturn(true);
        when(operationRepository.findById("OP-2")).thenReturn(Optional.of(rejectedOperation("OP-2", "BEN002")));

        mockMvc.perform(get("/api/portabilidades/OP-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operacaoId").value("OP-2"))
                .andExpect(jsonPath("$.beneficiarioId").value("BEN002"))
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.motivoRejeicao").value("Beneficiário atingiu o limite de margem consignável"))
                .andExpect(jsonPath("$.criadaEm").value("2026-10-29T21:45:00Z"))
                .andExpect(jsonPath("$.valorTotal").value(300.00))
                .andExpect(jsonPath("$.parcelas[0].parcelaId").value("P010"))
                .andExpect(jsonPath("$.contaDestino.banco").value("001"))
                .andExpect(jsonPath("$.eventos.length()").value(3))
                .andExpect(jsonPath("$.eventos[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$.eventos[0].descricao").value("Solicitação registrada"))
                .andExpect(jsonPath("$.eventos[2].status").value("REJEITADA"))
                .andExpect(jsonPath("$.eventos[2].ocorridoEm").isNotEmpty());
    }

    @Test
    void returns404ForUnknownOperation() throws Exception {
        when(operationRepository.findById("OP-X")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/portabilidades/OP-X"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
    }

    @Test
    void listsOperationsInContractFormat() throws Exception {
        when(currentUser.isOperator()).thenReturn(true);
        when(operationRepository.findAll(null, null)).thenReturn(List.of(rejectedOperation("OP-2", "BEN002")));

        mockMvc.perform(get("/api/portabilidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].operacaoId").value("OP-2"))
                .andExpect(jsonPath("$[0].beneficiarioId").value("BEN002"))
                .andExpect(jsonPath("$[0].status").value("REJEITADA"))
                .andExpect(jsonPath("$[0].motivoRejeicao").value("Beneficiário atingiu o limite de margem consignável"))
                .andExpect(jsonPath("$[0].criadaEm").value("2026-10-29T21:45:00Z"))
                .andExpect(jsonPath("$[0].quantidadeParcelas").value(1))
                .andExpect(jsonPath("$[0].valorTotal").value(300.00));
    }

    @Test
    void returnsSummaryInsteadOfTreatingResumoAsAnId() throws Exception {
        when(currentUser.isOperator()).thenReturn(true);
        when(operationRepository.findAll(null, null)).thenReturn(List.of(rejectedOperation("OP-2", "BEN002")));

        mockMvc.perform(get("/api/portabilidades/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pendentes").value(0))
                .andExpect(jsonPath("$.confirmadas").value(0))
                .andExpect(jsonPath("$.rejeitadas").value(1))
                .andExpect(jsonPath("$.valorTotalConfirmado").value(0));
    }

    @Test
    void rejectsUnknownStatusFilter() throws Exception {
        mockMvc.perform(get("/api/portabilidades").param("status", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    private Operation rejectedOperation(String operationId, String beneficiaryId) {
        Operation operation = new Operation();
        operation.setOperationId(operationId);
        operation.setBeneficiaryId(beneficiaryId);
        operation.setCentralProtocol("PROTO-2");
        operation.setCreatedAt(Instant.parse("2026-10-29T21:45:00Z"));
        operation.setTotalAmount(new BigDecimal("300.00"));
        operation.setInstallments(List.of(
                new Installment("P010", "CONV02", new BigDecimal("300.00"), LocalDate.of(2026, 1, 15), "ABERTA")));
        operation.setDestinationAccount(new DestinationAccount("001", "1234", "56789-0"));
        operation.setRejectionReason("Beneficiário atingiu o limite de margem consignável");
        operation.changeStatus(OperationStatus.ENVIANDO, "Solicitação registrada");
        operation.changeStatus(OperationStatus.PENDENTE, "Aceita pela Central");
        operation.changeStatus(OperationStatus.REJEITADA, "Rejeitada pela Central");
        return operation;
    }

}
