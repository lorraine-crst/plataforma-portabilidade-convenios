package br.com.nuvola.portability.schedule;

import br.com.nuvola.portability.central.CentralInstallment;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentResponse(
        String parcelaId,
        String convenioId,
        BigDecimal valor,
        LocalDate dataDesconto,
        String status) {

    public static InstallmentResponse from(CentralInstallment installment) {
        return new InstallmentResponse(
                installment.parcelaId(),
                installment.convenioId(),
                installment.valor(),
                installment.dataDesconto(),
                installment.status());
    }

}
