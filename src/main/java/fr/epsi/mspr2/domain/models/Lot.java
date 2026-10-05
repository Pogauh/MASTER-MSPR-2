package fr.epsi.mspr2.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Lot de café stocké dans une zone d'un entrepôt, issu d'une exploitation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lot {

    /** Un lot est périmé quand il a été produit il y a plus d'un an. */
    private static final int DUREE_DE_VIE_EN_ANNEES = 1;

    private UUID idLot;
    private StatusEnum status;
    private Integer quantite;
    private Integer caracteristique;
    private LocalDateTime dateSortie;
    private LocalDateTime dateStockage;
    private LocalDate dateProduction;
    private Exploitation exploitation;
    private Zone zone;
    @Builder.Default
    private List<Historique> historique = new ArrayList<>();

    /** L'entrepôt d'un lot est celui de la zone dans laquelle il est stocké. */
    public Entrepot getEntrepot() {
        return zone != null ? zone.getEntrepot() : null;
    }

    /** Entrée en stock : la date de stockage est celle du jour et un lot sans statut est conforme. */
    public void enregistrerStockage(LocalDateTime date) {
        if (status == null) {
            status = StatusEnum.COMPLIANT;
        }
        dateStockage = date;
    }

    /** Les lots produits avant cette date sont périmés. */
    public static LocalDate dateLimiteDeProduction(LocalDate aujourdhui) {
        return aujourdhui.minusYears(DUREE_DE_VIE_EN_ANNEES);
    }

    public boolean estExpire(LocalDate aujourdhui) {
        return dateProduction != null
                && dateProduction.isBefore(dateLimiteDeProduction(aujourdhui))
                && status != StatusEnum.EXPIRED;
    }

    public void passerEnAlerte() {
        status = StatusEnum.ALERT;
    }

    public void expirer() {
        status = StatusEnum.EXPIRED;
    }

    public void expedier(LocalDateTime date) {
        status = StatusEnum.SHIPPED;
        dateSortie = date;
    }

    public void deplacerVers(UUID idZone) {
        zone = Zone.builder().idZone(idZone).build();
    }
}
