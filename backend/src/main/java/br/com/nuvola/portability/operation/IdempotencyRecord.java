package br.com.nuvola.portability.operation;

import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class IdempotencyRecord {

	private String key;
	private String operationId;
	private String requestHash;
	private Instant createdAt;

	public IdempotencyRecord() {
	}

	public IdempotencyRecord(String key, String operationId, String requestHash, Instant createdAt) {
		this.key = key;
		this.operationId = operationId;
		this.requestHash = requestHash;
		this.createdAt = createdAt;
	}

	@DynamoDbPartitionKey
	@DynamoDbAttribute("chave")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@DynamoDbAttribute("operacaoId")
	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	@DynamoDbAttribute("hashRequisicao")
	public String getRequestHash() {
		return requestHash;
	}

	public void setRequestHash(String requestHash) {
		this.requestHash = requestHash;
	}

	@DynamoDbAttribute("criadaEm")
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

}
