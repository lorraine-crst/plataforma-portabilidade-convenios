package br.com.nuvola.portability.schedule;

import br.com.nuvola.portability.authorization.AuthorizationService;
import br.com.nuvola.portability.central.CentralClient;
import br.com.nuvola.portability.central.CentralInstallment;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

    private final AuthorizationService authorizationService;
    private final CentralClient centralClient;

    public ScheduleService(AuthorizationService authorizationService, CentralClient centralClient) {
        this.authorizationService = authorizationService;
        this.centralClient = centralClient;
    }

    public List<CentralInstallment> findSchedule(String beneficiaryId) {
        authorizationService.ensureAuthorized(beneficiaryId);
        return centralClient.getSchedule(beneficiaryId);
    }

}
