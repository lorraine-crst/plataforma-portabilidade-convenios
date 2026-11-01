package br.com.nuvola.portability.operation;

import br.com.nuvola.portability.central.CentralClient;
import br.com.nuvola.portability.central.CentralDestinationAccount;
import br.com.nuvola.portability.central.CentralInstallment;
import br.com.nuvola.portability.central.CentralOperationRequest;
import br.com.nuvola.portability.error.CentralUnavailableException;
import br.com.nuvola.portability.error.IdempotencyConflictException;
import br.com.nuvola.portability.error.InvalidInstallmentException;
import br.com.nuvola.portability.error.OperationNotFoundException;
import br.com.nuvola.portability.error.UncertainSubmissionException;
import br.com.nuvola.portability.schedule.ScheduleService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PortabilityService {

    private static final String OPEN = "ABERTA";

    private final ScheduleService scheduleService;
    private final CentralClient centralClient;
    private final OperationRepository operationRepository;
    private final IdempotencyRepository idempotencyRepository;

    public PortabilityService(ScheduleService scheduleService, CentralClient centralClient,
            OperationRepository operationRepository, IdempotencyRepository idempotencyRepository) {
        this.scheduleService = scheduleService;
        this.centralClient = centralClient;
        this.operationRepository = operationRepository;
        this.idempotencyRepository = idempotencyRepository;
    }

    public Operation create(String idempotencyKey, PortabilityRequest request) {
        String requestHash = RequestFingerprint.of(request);

        Optional<IdempotencyRecord> existing = idempotencyRepository.findByKey(idempotencyKey);
        if (existing.isPresent()) {
            return handleRepeated(existing.get(), requestHash);
        }

        Operation operation = newOperation(idempotencyKey, request);
        operationRepository.save(operation);

        IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, operation.getOperationId(), requestHash,
                Instant.now());
        if (!idempotencyRepository.reserve(record)) {
            operationRepository.delete(operation.getOperationId());
            return handleRepeated(idempotencyRepository.findByKey(idempotencyKey).orElseThrow(), requestHash);
        }

        return send(operation);
    }

    private Operation handleRepeated(IdempotencyRecord record, String requestHash) {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new IdempotencyConflictException();
        }
        Operation operation = operationRepository.findById(record.getOperationId())
                .orElseThrow(() -> new OperationNotFoundException(record.getOperationId()));
        if (operation.getStatus() == OperationStatus.FALHA_ENVIO) {
            operation.changeStatus(OperationStatus.ENVIANDO, "Nova tentativa de envio");
            operationRepository.save(operation);
            return send(operation);
        }
        return operation;
    }

    private Operation newOperation(String idempotencyKey, PortabilityRequest request) {
        List<Installment> installments = selectInstallments(request);

        Operation operation = new Operation();
        operation.setOperationId(UUID.randomUUID().toString());
        operation.setBeneficiaryId(request.beneficiarioId());
        operation.setIdempotencyKey(idempotencyKey);
        operation.setInstallments(installments);
        operation.setDestinationAccount(request.contaDestino().toDestinationAccount());
        operation.setTotalAmount(installments.stream()
                .map(Installment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        operation.setCreatedAt(Instant.now());
        operation.changeStatus(OperationStatus.ENVIANDO, "Solicitação registrada");
        return operation;
    }

    private List<Installment> selectInstallments(PortabilityRequest request) {
        Map<String, CentralInstallment> schedule = scheduleService.findSchedule(request.beneficiarioId()).stream()
                .collect(Collectors.toMap(CentralInstallment::parcelaId, Function.identity()));
        return request.normalizedInstallmentIds().stream()
                .map(installmentId -> validInstallment(schedule, installmentId))
                .toList();
    }

    private Installment validInstallment(Map<String, CentralInstallment> schedule, String installmentId) {
        CentralInstallment installment = schedule.get(installmentId);
        if (installment == null) {
            throw new InvalidInstallmentException(
                    "A parcela " + installmentId + " não existe na agenda do beneficiário.");
        }
        if (!OPEN.equals(installment.status())) {
            throw new InvalidInstallmentException(
                    "A parcela " + installmentId + " não está ABERTA (status atual: " + installment.status() + ").");
        }
        return new Installment(installment.parcelaId(), installment.convenioId(), installment.valor(),
                installment.dataDesconto(), installment.status());
    }

    private Operation send(Operation operation) {
        try {
            String protocol = centralClient.createOperation(toCentralRequest(operation));
            operation.setCentralProtocol(protocol);
            operation.changeStatus(OperationStatus.PENDENTE, "Aceita pela Central");
            operationRepository.save(operation);
            return operation;
        } catch (CentralUnavailableException e) {
            operation.changeStatus(OperationStatus.FALHA_ENVIO, "Central indisponível; a solicitação não foi enviada");
            operationRepository.save(operation);
            throw e;
        } catch (UncertainSubmissionException e) {
            operation.changeStatus(OperationStatus.ENVIO_INCERTO, "A Central não respondeu a tempo");
            operationRepository.save(operation);
            throw e;
        }
    }

    private CentralOperationRequest toCentralRequest(Operation operation) {
        DestinationAccount account = operation.getDestinationAccount();
        List<String> installmentIds = operation.getInstallments().stream()
                .map(Installment::getInstallmentId)
                .toList();
        return new CentralOperationRequest(operation.getBeneficiaryId(), installmentIds,
                new CentralDestinationAccount(account.getBank(), account.getBranch(), account.getAccount()));
    }

}
