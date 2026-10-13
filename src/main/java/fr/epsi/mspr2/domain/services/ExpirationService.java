package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.domain.ports.NotificationPort;
import fr.epsi.mspr2.domain.ports.TransactionPort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Détecte les lots périmés (stockés depuis plus d'un an) : ils passent au statut EXPIRED et une alerte est levée pour chacun. */
@RequiredArgsConstructor
public class ExpirationService {

    private final LotPort lotPort;
    private final AlertePort alertePort;
    private final HistoriquePort historiquePort;
    private final NotificationPort notificationPort;
    private final TransactionPort transactionPort;
    private final Clock clock;

    /** @return le nombre de lots qui viennent d'expirer */
    public int expirerLotsPerimes() {
        LocalDate aujourdhui = LocalDate.now(clock);
        var lotsExpires = lotPort.findByDateStockageBefore(Lot.dateLimiteDeStockage(aujourdhui).atStartOfDay()).stream()
                .filter(lot -> lot.estExpire(aujourdhui))
                .toList();
        lotsExpires.forEach(this::declarerExpire);
        return lotsExpires.size();
    }

    private void declarerExpire(Lot lot) {
        LocalDateTime maintenant = LocalDateTime.now(clock);
        Alerte alerte = transactionPort.execute(() -> {
            Alerte enregistree = alertePort.save(Alerte.builder()
                    .typeAlerte(TypeAlerteEnum.EXPIRED)
                    .description("Le lot " + lot.getIdLot() + " a expiré.")
                    .dateEmission(maintenant)
                    .build());
            lot.expirer();
            lotPort.save(lot);
            historiquePort.save(Historique.alerte(lot, "Lot expiré", maintenant));
            return enregistree;
        });
        notificationPort.notifierAlerte(alerte);
    }
}
