package br.com.nuvola.portability.schedule;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nuvola.portability.central.CentralInstallment;
import br.com.nuvola.portability.error.BeneficiaryNotAuthorizedException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    @Test
    void returns422WhenBeneficiaryHasNotAuthorized() throws Exception {
        when(scheduleService.findSchedule("BEN003")).thenThrow(new BeneficiaryNotAuthorizedException("BEN003"));

        mockMvc.perform(get("/api/beneficiarios/BEN003/agenda"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("BENEFICIARIO_NAO_AUTORIZADO"));
    }

    @Test
    void returnsInstallmentsInContractFormat() throws Exception {
        when(scheduleService.findSchedule("BEN001")).thenReturn(List.of(
                new CentralInstallment("P001", "CONV01", new BigDecimal("150.00"), LocalDate.of(2026, 1, 10), "ABERTA")));

        mockMvc.perform(get("/api/beneficiarios/BEN001/agenda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].parcelaId").value("P001"))
                .andExpect(jsonPath("$[0].convenioId").value("CONV01"))
                .andExpect(jsonPath("$[0].valor").value(150.00))
                .andExpect(jsonPath("$[0].dataDesconto").value("2026-01-10"))
                .andExpect(jsonPath("$[0].status").value("ABERTA"));
    }

    @Test
    void returnsEmptyListWhenScheduleIsEmpty() throws Exception {
        when(scheduleService.findSchedule("BEN_ERRO")).thenReturn(List.of());

        mockMvc.perform(get("/api/beneficiarios/BEN_ERRO/agenda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

}
