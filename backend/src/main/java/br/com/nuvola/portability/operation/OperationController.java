package br.com.nuvola.portability.operation;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portabilidades")
public class OperationController {

    private final OperationTrackingService trackingService;

    public OperationController(OperationTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping
    public List<OperationListItemResponse> list(
            @RequestParam(required = false) OperationStatus status,
            @RequestParam(name = "beneficiarioId", required = false) String beneficiaryId) {
        return trackingService.list(status, beneficiaryId).stream()
                .map(OperationListItemResponse::from)
                .toList();
    }

    @GetMapping("/resumo")
    public OperationSummaryResponse summary() {
        return trackingService.summarize();
    }

    @GetMapping("/{operationId}")
    public OperationResponse findById(@PathVariable String operationId) {
        return OperationResponse.from(trackingService.findById(operationId));
    }

}
