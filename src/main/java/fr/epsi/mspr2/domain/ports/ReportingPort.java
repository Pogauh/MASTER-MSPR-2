package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;

import java.time.LocalDateTime;

public interface ReportingPort {

    ReportingGlobal generateReportingGlobal();

    ReportingQuality generateReportingQuality(LocalDateTime startDate, LocalDateTime endDate);

}
