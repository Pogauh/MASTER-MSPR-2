package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.ReportingPort;
import fr.epsi.mspr2.infrastructure.jpa.JpaAlerteRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReportingAdapter implements ReportingPort {
    private final JpaLotRepository jpaLotRepository;
    private final JpaHistoriqueRepository jpaHistoriqueRepository;
    private final JpaAlerteRepository jpaAlerteRepository;

    public ReportingAdapter(JpaLotRepository jpaLotRepository, JpaHistoriqueRepository jpaHistoriqueRepository, JpaAlerteRepository jpaAlerteRepository) {
        this.jpaLotRepository = jpaLotRepository;
        this.jpaHistoriqueRepository = jpaHistoriqueRepository;
        this.jpaAlerteRepository = jpaAlerteRepository;
    }

    public ReportingGlobal generateReportingGlobal() {
        var totalLots = jpaLotRepository.count();
        var compliantRate = jpaLotRepository.countByStatusIn(List.of(StatusEnum.COMPLIANT)) / (double) totalLots * 100;

        // faire la moyenne de l'age des lots en jours à partir de dateStockage à aujourd'hui
        var avgAgeDays = jpaLotRepository.findAverageAgeInDays();

        // Récupérer tout les Historique de type TRANSFERT généré les 30 derniers jours
        var mouvementLast30Days = jpaHistoriqueRepository.countByDateEmissionAfterAndType(LocalDateTime.now().minusDays(30), TypeHistoriqueEnum.TRANSFERT);

        ReportingGlobal reportingGlobal = new ReportingGlobal();
        reportingGlobal.setTotalLots((int) totalLots);
        reportingGlobal.setComplianceRate(compliantRate);
        reportingGlobal.setAvgAgeDays(avgAgeDays);
        reportingGlobal.setCountMouvementLast30Days(mouvementLast30Days);
        return reportingGlobal;
    }

    @Override
    public ReportingQuality generateReportingQuality(LocalDateTime startDate, LocalDateTime endDate) {
        var totalLots = jpaLotRepository.count();
        var compliantRate = jpaLotRepository.countByStatusIn(List.of(StatusEnum.COMPLIANT)) / (double) totalLots * 100;
        var nonComplianceCount = jpaLotRepository.countByStatusIn(List.of(StatusEnum.ALERT, StatusEnum.EXPIRED));
        var alertCount = jpaAlerteRepository.countByDateEmissionBetween(startDate, endDate);

        ReportingQuality reportingQuality = new ReportingQuality();
        reportingQuality.setAlertCount(alertCount);
        reportingQuality.setComplianceRate(compliantRate);
        reportingQuality.setNonComplianceCount(nonComplianceCount);
        return reportingQuality;
    }
}
