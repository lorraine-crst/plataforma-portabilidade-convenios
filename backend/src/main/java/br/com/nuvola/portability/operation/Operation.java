package br.com.nuvola.portability.operation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Operation {

	private String operationId;
	private String beneficiaryId;
	private String centralProtocol;
	private OperationStatus status;
	private String rejectionReason;
	private List<Installment> installments = new ArrayList<>();
	private DestinationAccount destinationAccount;
	private BigDecimal totalAmount;
	private String idempotencyKey;
	private Instant createdAt;
	private Instant updatedAt;
	private List<Event> events = new ArrayList<>();

	public Operation() {
	}

	@DynamoDbPartitionKey
	@DynamoDbAttribute("operacaoId")
	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	@DynamoDbAttribute("beneficiarioId")
	public String getBeneficiaryId() {
		return beneficiaryId;
	}

	public void setBeneficiaryId(String beneficiaryId) {
		this.beneficiaryId = beneficiaryId;
	}

	@DynamoDbAttribute("protocoloCentral")
	public String getCentralProtocol() {
		return centralProtocol;
	}

	public void setCentralProtocol(String centralProtocol) {
		this.centralProtocol = centralProtocol;
	}

	@DynamoDbAttribute("status")
	public OperationStatus getStatus() {
		return status;
	}

	public void setStatus(OperationStatus status) {
		this.status = status;
	}

	@DynamoDbAttribute("motivoRejeicao")
	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	@DynamoDbAttribute("parcelas")
	public List<Installment> getInstallments() {
		return installments;
	}

	public void setInstallments(List<Installment> installments) {
		this.installments = installments;
	}

	@DynamoDbAttribute("contaDestino")
	public DestinationAccount getDestinationAccount() {
		return destinationAccount;
	}

	public void setDestinationAccount(DestinationAccount destinationAccount) {
		this.destinationAccount = destinationAccount;
	}

	@DynamoDbAttribute("valorTotal")
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	@DynamoDbAttribute("idempotencyKey")
	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}

	@DynamoDbAttribute("criadaEm")
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	@DynamoDbAttribute("atualizadaEm")
	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	@DynamoDbAttribute("eventos")
	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

}
