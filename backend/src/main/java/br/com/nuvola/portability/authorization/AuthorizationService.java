package br.com.nuvola.portability.authorization;

import br.com.nuvola.portability.central.CentralAuthorizationResponse;
import br.com.nuvola.portability.central.CentralClient;
import br.com.nuvola.portability.error.AuthorizationNotFoundException;
import br.com.nuvola.portability.error.BeneficiaryNotAuthorizedException;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private final AuthorizationRepository repository;
    private final CentralClient centralClient;

    public AuthorizationService(AuthorizationRepository repository, CentralClient centralClient) {
        this.repository = repository;
        this.centralClient = centralClient;
    }

    public AuthorizationResult authorize(String beneficiaryId) {
        return repository.findByBeneficiaryId(beneficiaryId)
                .map(existing -> new AuthorizationResult(existing, false))
                .orElseGet(() -> register(beneficiaryId));
    }

    public Authorization findMine(String beneficiaryId) {
        return repository.findByBeneficiaryId(beneficiaryId)
                .orElseThrow(AuthorizationNotFoundException::new);
    }

    public void ensureAuthorized(String beneficiaryId) {
        if (repository.findByBeneficiaryId(beneficiaryId).isEmpty()) {
            throw new BeneficiaryNotAuthorizedException(beneficiaryId);
        }
    }

    private AuthorizationResult register(String beneficiaryId) {
        CentralAuthorizationResponse response = centralClient.registerAuthorization(beneficiaryId);
        Authorization authorization = new Authorization(beneficiaryId, response.autorizacaoId(), Instant.now());
        repository.save(authorization);
        return new AuthorizationResult(authorization, true);
    }

}
