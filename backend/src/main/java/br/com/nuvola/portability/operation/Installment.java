package br.com.nuvola.portability.operation;

import java.math.BigDecimal;
import java.time.LocalDate;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class Installment {

	private String installmentId;
	private String agreementId;
	private BigDecimal amount;
	private LocalDate deductionDate;
	private String status;

	public Installment() {
	}

	public Installment(String installmentId, String agreementId, BigDecimal amount, LocalDate deductionDate, String status) {
		this.installmentId = installmentId;
		this.agreementId = agreementId;
		this.amount = amount;
		this.deductionDate = deductionDate;
		this.status = status;
	}

	@DynamoDbAttribute("parcelaId")
	public String getInstallmentId() {
		return installmentId;
	}

	public void setInstallmentId(String installmentId) {
		this.installmentId = installmentId;
	}

	@DynamoDbAttribute("convenioId")
	public String getAgreementId() {
		return agreementId;
	}

	public void setAgreementId(String agreementId) {
		this.agreementId = agreementId;
	}

	@DynamoDbAttribute("valor")
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@DynamoDbAttribute("dataDesconto")
	public LocalDate getDeductionDate() {
		return deductionDate;
	}

	public void setDeductionDate(LocalDate deductionDate) {
		this.deductionDate = deductionDate;
	}

	@DynamoDbAttribute("status")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
