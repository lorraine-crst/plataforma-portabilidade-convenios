package br.com.nuvola.portability.error;

import org.springframework.http.HttpStatus;

public class OperationAccessDeniedException extends ApiException {

    public OperationAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Você não tem permissão para ver esta operação.");
    }

}
