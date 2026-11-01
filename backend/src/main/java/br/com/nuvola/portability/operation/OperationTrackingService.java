package br.com.nuvola.portability.operation;

import br.com.nuvola.portability.auth.CurrentUser;
import br.com.nuvola.portability.central.CentralClient;
import br.com.nuvola.portability.central.CentralOperationStatus;
import br.com.nuvola.portability.error.CentralUnavailableException;
import br.com.nuvola.portability.error.OperationAccessDeniedException;
import br.com.nuvola.portability.error.OperationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OperationTrackingService {

    private static final Logger log = LoggerFactory.getLogger(OperationTrackingService.class);

    private final OperationRepository operationRepository;
    private final CentralClient centralClient;
    private final CurrentUser currentUser;

    public OperationTrackingService(OperationRepository operationRepository, CentralClient centralClient,
            CurrentUser currentUser) {
        this.operationRepository = operationRepository;
        this.centralClient = centralClient;
        this.currentUser = currentUser;
    }

    public Operation findById(String operationId) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new OperationNotFoundException(operationId));
        ensureCanView(operation);
        return refresh(operation);
    }

    private void ensureCanView(Operation operation) {
        if (!currentUser.isOperator() && !operation.getBeneficiaryId().equals(currentUser.username())) {
            throw new OperationAccessDeniedException();
        }
    }

    private Operation refresh(Operation operation) {
        if (operation.getStatus() != OperationStatus.PENDENTE) {
            return operation;
        }
        try {
            CentralOperationStatus result = centralClient.getOperationStatus(operation.getCentralProtocol());
            if (result.status() == OperationStatus.REJEITADA) {
                operation.setRejectionReason(result.rejectionReason());
            }
            if (operation.changeStatus(result.status(), describe(result.status()))) {
                operationRepository.save(operation);
            }
        } catch (CentralUnavailableException e) {
            log.warn("Central indisponível ao atualizar a operação {}", operation.getOperationId());
        }
        return operation;
    }

    private String describe(OperationStatus status) {
        return switch (status) {
            case CONFIRMADA -> "Confirmada pela Central";
            case REJEITADA -> "Rejeitada pela Central";
            case NAO_ENCONTRADA_NA_CENTRAL -> "A Central não encontrou a solicitação";
            default -> "Atualizada pela Central";
        };
    }

}
