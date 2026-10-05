package fr.epsi.mspr2.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** Événement de la vie d'un lot : transfert de zone, alerte ou expédition. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Historique {
    private UUID idHistorique;
    private TypeHistoriqueEnum type;
    private LocalDateTime dateEmission;
    private UUID zonePrecedente;
    private UUID zoneActuelle;
    private String detail;
    private UUID idLot;

    /** Transfert du lot vers une autre zone. */
    public static Historique transfert(Lot lot, UUID idZoneDestination, LocalDateTime date) {
        UUID idZoneOrigine = lot.getZone().getIdZone();
        return Historique.builder()
                .type(TypeHistoriqueEnum.TRANSFERT)
                .dateEmission(date)
                .zonePrecedente(idZoneOrigine)
                .zoneActuelle(idZoneDestination)
                .detail("Transfert du lot " + lot.getIdLot() + " de la zone " + idZoneOrigine + " vers la zone " + idZoneDestination)
                .idLot(lot.getIdLot())
                .build();
    }

    /** Alerte ayant touché le lot : il reste dans sa zone. */
    public static Historique alerte(Lot lot, String detail, LocalDateTime date) {
        UUID idZone = lot.getZone().getIdZone();
        return Historique.builder()
                .type(TypeHistoriqueEnum.ALERTE)
                .dateEmission(date)
                .zonePrecedente(idZone)
                .zoneActuelle(idZone)
                .detail(detail)
                .idLot(lot.getIdLot())
                .build();
    }

    /** Sortie de stock (expédition) du lot. */
    public static Historique expedition(UUID idLot, LocalDateTime date) {
        return Historique.builder()
                .type(TypeHistoriqueEnum.SHIPPED)
                .dateEmission(date)
                .detail("Le lot a été expédié.")
                .idLot(idLot)
                .build();
    }
}
