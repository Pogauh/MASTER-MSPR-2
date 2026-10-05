package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ReportingService {

    private static final int JOURS_DE_MOUVEMENTS_COMPTES = 30;

    private final LotPort lotPort;
    private final HistoriquePort historiquePort;
    private final AlertePort alertePort;
    private final Clock clock;

    public ReportingGlobal getReportingGlobal() {
        long totalLots = lotPort.count();
        long mouvements = historiquePort.countByTypeAfter(TypeHistoriqueEnum.TRANSFERT,
                LocalDateTime.now(clock).minusDays(JOURS_DE_MOUVEMENTS_COMPTES));
        return new ReportingGlobal((int) totalLots, ageMoyenEnJours(), tauxDeConformite(totalLots), (int) mouvements);
    }

    public ReportingQuality getReportingQuality(LocalDateTime startDate, LocalDateTime endDate) {
        long totalLots = lotPort.count();
        long nonConformes = lotPort.countByStatusIn(List.of(StatusEnum.ALERT, StatusEnum.EXPIRED));
        int alertes = alertePort.countByDateEmissionBetween(startDate, endDate);
        return new ReportingQuality(alertes, tauxDeConformite(totalLots), (int) nonConformes);
    }

    /**
     * Situation du stock courant. Les lots « à écouler en priorité » (FIFO) sont ceux stockés depuis
     * 9 à 12 mois.
     */
    public ReportingStock getReportingStock() {
        long totalLots = lotPort.count();
        LocalDateTime maintenant = LocalDateTime.now(clock);
        List<UUID> aEcoulerEnPriorite = lotPort.findIdsByDateStockageBetween(maintenant.minusYears(1), maintenant.minusMonths(9));
        return new ReportingStock((int) totalLots, ageMoyenEnJours(), tauxDeConformite(totalLots), aEcoulerEnPriorite);
    }

    private double ageMoyenEnJours() {
        return lotPort.findAverageAgeInDays().orElse(0);
    }

    /** Pourcentage de lots conformes ; 0 quand il n'y a aucun lot. */
    private double tauxDeConformite(long totalLots) {
        if (totalLots == 0) {
            return 0;
        }
        return lotPort.countByStatusIn(List.of(StatusEnum.COMPLIANT)) / (double) totalLots * 100;
    }
}
