package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.services.ReportingService;
import fr.epsi.mspr2.infrastructure.dto.ReportingGlobalDto;
import fr.epsi.mspr2.infrastructure.dto.ReportingQualityDto;
import fr.epsi.mspr2.infrastructure.dto.ReportingStockDto;
import fr.epsi.mspr2.infrastructure.mapper.ReportingMapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@AllArgsConstructor
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
        return reportingMapper.toReportingQualityDto(reportingService.getReportingQuality(startDate, endDate));
    }

    @GetMapping("/stock")
    public ReportingStockDto getStockReport(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return reportingMapper.toReportingStockDto(reportingService.getReportingStock(startDate, endDate));
    }
}
