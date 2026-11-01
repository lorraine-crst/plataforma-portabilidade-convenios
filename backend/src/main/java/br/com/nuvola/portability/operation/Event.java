package br.com.nuvola.portability.operation;

import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class Event {

	private OperationStatus status;
	private Instant occurredAt;
	private String description;

	public Event() {
	}

	public Event(OperationStatus status, Instant occurredAt, String description) {
		this.status = status;
		this.occurredAt = occurredAt;
		this.description = description;
	}

	@DynamoDbAttribute("status")
	public OperationStatus getStatus() {
		return status;
	}

	public void setStatus(OperationStatus status) {
		this.status = status;
	}

	@DynamoDbAttribute("ocorridoEm")
	public Instant getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(Instant occurredAt) {
		this.occurredAt = occurredAt;
	}

	@DynamoDbAttribute("descricao")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
