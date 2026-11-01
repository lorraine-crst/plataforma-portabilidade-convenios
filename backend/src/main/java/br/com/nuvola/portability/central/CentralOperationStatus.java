package br.com.nuvola.portability.central;

import br.com.nuvola.portability.operation.OperationStatus;

public record CentralOperationStatus(OperationStatus status, String rejectionReason) {
}
