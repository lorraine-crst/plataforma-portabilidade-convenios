package br.com.nuvola.portability.operation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portabilidades")
public class PortabilityController {

    private final PortabilityService portabilityService;

    public PortabilityController(PortabilityService portabilityService) {
        this.portabilityService = portabilityService;
    }

    @PostMapping
    public ResponseEntity<CreatePortabilityResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PortabilityRequest request) {
        Operation operation = portabilityService.create(idempotencyKey, request);
        return ResponseEntity.accepted().body(CreatePortabilityResponse.from(operation));
    }

}
