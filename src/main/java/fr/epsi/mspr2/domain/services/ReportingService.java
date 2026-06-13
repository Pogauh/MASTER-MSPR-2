package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.domain.ports.ReportingPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class ReportingService {

    private final ReportingPort reportingPort;

    public ReportingGlobal getReportingGlobal() {
        return reportingPort.generateReportingGlobal();
    }

    public ReportingQuality getReportingQuality(LocalDateTime startDate, LocalDateTime endDate) {
        return reportingPort.generateReportingQuality(startDate, endDate);
    }

    public ReportingStock getReportingStock(LocalDateTime startDate, LocalDateTime endDate) {
        return reportingPort.generateReportingStock(startDate, endDate);
    }
}