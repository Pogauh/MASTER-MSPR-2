package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.dto.ReportingGlobalDto;
import fr.epsi.mspr2.api.dto.ReportingQualityDto;
import fr.epsi.mspr2.api.dto.ReportingStockDto;
import fr.epsi.mspr2.api.mapper.ReportingMapper;
import fr.epsi.mspr2.domain.services.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    private final ReportingService reportingService;
    private final ReportingMapper reportingMapper;

    @GetMapping("/global")
    public ReportingGlobalDto getGlobalReport() {
        return reportingMapper.toDto(reportingService.getReportingGlobal());
    }

    @GetMapping("/quality")
    public ReportingQualityDto getQualityReport(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return reportingMapper.toDto(reportingService.getReportingQuality(startDate, endDate));
    }

    /**
     * Les dates sont exigées par l'API mais le rapport porte toujours sur le stock courant :
     * elles ne filtrent pas le résultat.
     */
    @GetMapping("/stock")
    public ReportingStockDto getStockReport(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return reportingMapper.toDto(reportingService.getReportingStock());
    }
}
