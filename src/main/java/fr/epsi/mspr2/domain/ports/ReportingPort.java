package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;

import java.time.LocalDateTime;

public interface ReportingPort {

    ReportingGlobal generateReportingGlobal();

    ReportingQuality generateReportingQuality(LocalDateTime startDate, LocalDateTime endDate);

    ReportingStock generateReportingStock(LocalDateTime startDate, LocalDateTime endDate);

}
