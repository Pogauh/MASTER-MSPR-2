package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.domain.ports.ReportingPort;
import fr.epsi.mspr2.infrastructure.jpa.JpaAlerteRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Override
    public ReportingStock generateReportingStock(LocalDateTime startDate, LocalDateTime endDate) {
        var totalLots = jpaLotRepository.count();
        var compliantRate = jpaLotRepository.countByStatusIn(List.of(StatusEnum.COMPLIANT)) / (double) totalLots * 100;
        var avgAgeDays = jpaLotRepository.findAverageAgeInDays();
        List<UUID> urgentFifo = jpaLotRepository.findIdsByDateStockageBetween(LocalDateTime.now().minusYears(1), LocalDateTime.now().minusMonths(9));

        ReportingStock reportingStock = new ReportingStock();
        reportingStock.setTotalLots((int) totalLots);
        reportingStock.setComplianceRate(compliantRate);
        reportingStock.setAvgAgeDays(avgAgeDays);
        reportingStock.setLotIdsUrgentFifo(urgentFifo);
        return reportingStock;
    }
}
