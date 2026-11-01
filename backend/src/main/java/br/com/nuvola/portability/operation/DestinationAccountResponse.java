package br.com.nuvola.portability.operation;

public record DestinationAccountResponse(String banco, String agencia, String conta) {

    public static DestinationAccountResponse from(DestinationAccount account) {
        return new DestinationAccountResponse(account.getBank(), account.getBranch(), account.getAccount());
    }

}
