package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class BeneficiaryNotAuthorizedException extends ApiException {

	public BeneficiaryNotAuthorizedException(String beneficiaryId) {
		super(HttpStatus.UNPROCESSABLE_CONTENT, "BENEFICIARIO_NAO_AUTORIZADO",
				"O beneficiário " + beneficiaryId + " ainda não registrou a autorização.");
	}

}
