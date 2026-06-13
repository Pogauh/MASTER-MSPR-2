package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.ReportingPort;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReportingAdapter implements ReportingPort {
    private final JpaLotRepository jpaLotRepository;
    private final JpaHistoriqueRepository jpaHistoriqueRepository;

    public ReportingAdapter(JpaLotRepository jpaLotRepository, JpaHistoriqueRepository jpaHistoriqueRepository) {
        this.jpaLotRepository = jpaLotRepository;
        this.jpaHistoriqueRepository = jpaHistoriqueRepository;
    }

    public ReportingGlobal generateReportingGlobal() {
        var totalLots = jpaLotRepository.count();
        var compliantRate = jpaLotRepository.countByStatus(StatusEnum.COMPLIANT) / (double) totalLots * 100;

        // faire la moyenne de l'age des lots en jours à partir de dateStockage à aujourd'hui
        var avgAgeDays = jpaLotRepository.findAverageAgeInDays();

        // Récupérer tout les Historique de type TRANSFERT généré les 30 derniers jours
        var mouvementLast30Days = jpaHistoriqueRepository.countByDateEmissionAfterAndType(LocalDateTime.now().minusDays(30), TypeHistoriqueEnum.TRANSFERT);

        ReportingGlobal reportinGlobal = new ReportingGlobal();
        reportinGlobal.setTotalLots((int) totalLots);
        reportinGlobal.setComplianceRate(compliantRate);
        reportinGlobal.setAvgAgeDays(avgAgeDays);
        reportinGlobal.setCountMouvementLast30Days(mouvementLast30Days);
        return reportinGlobal;
    }
}
