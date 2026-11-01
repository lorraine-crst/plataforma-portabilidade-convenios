package br.com.nuvola.portability.authorization;

import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Authorization {

	private String beneficiaryId;
	private String authorizationId;
	private Instant authorizedAt;

	public Authorization() {
	}

	public Authorization(String beneficiaryId, String authorizationId, Instant authorizedAt) {
		this.beneficiaryId = beneficiaryId;
		this.authorizationId = authorizationId;
		this.authorizedAt = authorizedAt;
	}

	@DynamoDbPartitionKey
	@DynamoDbAttribute("beneficiarioId")
	public String getBeneficiaryId() {
		return beneficiaryId;
	}

	public void setBeneficiaryId(String beneficiaryId) {
		this.beneficiaryId = beneficiaryId;
	}

	@DynamoDbAttribute("autorizacaoId")
	public String getAuthorizationId() {
		return authorizationId;
	}

	public void setAuthorizationId(String authorizationId) {
		this.authorizationId = authorizationId;
	}

	@DynamoDbAttribute("autorizadaEm")
	public Instant getAuthorizedAt() {
		return authorizedAt;
	}

	public void setAuthorizedAt(Instant authorizedAt) {
		this.authorizedAt = authorizedAt;
	}

}
