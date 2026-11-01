package br.com.nuvola.portability.operation;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class DestinationAccount {

	private String bank;
	private String branch;
	private String account;

	public DestinationAccount() {
	}

	public DestinationAccount(String bank, String branch, String account) {
		this.bank = bank;
		this.branch = branch;
		this.account = account;
	}

	@DynamoDbAttribute("banco")
	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	@DynamoDbAttribute("agencia")
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@DynamoDbAttribute("conta")
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

}
