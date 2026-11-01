package br.com.nuvola.portability.operation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portabilidades")
public class OperationController {

    private final OperationTrackingService trackingService;

    public OperationController(OperationTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/{operationId}")
    public OperationResponse findById(@PathVariable String operationId) {
        return OperationResponse.from(trackingService.findById(operationId));
    }

}
