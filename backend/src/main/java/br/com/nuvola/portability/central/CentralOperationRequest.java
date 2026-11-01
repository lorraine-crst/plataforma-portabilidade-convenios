package br.com.nuvola.portability.central;

import java.util.List;

public record CentralOperationRequest(
		String beneficiarioId,
		List<String> parcelaIds,
		CentralDestinationAccount contaDestino) {
}
