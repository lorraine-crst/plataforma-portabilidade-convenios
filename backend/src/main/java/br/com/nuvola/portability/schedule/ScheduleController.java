package br.com.nuvola.portability.schedule;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/beneficiarios")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/{beneficiaryId}/agenda")
    public List<InstallmentResponse> findSchedule(@PathVariable String beneficiaryId) {
        return scheduleService.findSchedule(beneficiaryId).stream()
                .map(InstallmentResponse::from)
                .toList();
    }

}
