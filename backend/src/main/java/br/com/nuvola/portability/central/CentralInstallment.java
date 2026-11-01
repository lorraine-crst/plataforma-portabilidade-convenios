package br.com.nuvola.portability.central;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CentralInstallment(
		String parcelaId,
		String convenioId,
		BigDecimal valor,
		LocalDate dataDesconto,
		String status) {
}
