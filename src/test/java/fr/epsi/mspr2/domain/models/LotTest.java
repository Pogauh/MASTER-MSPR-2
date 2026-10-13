package fr.epsi.mspr2.domain.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static fr.epsi.mspr2.domain.support.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;

class LotTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 3, 15);

    @Test
    void unNouveauLotNAPasDHistorique() {
        assertThat(new Lot().getHistorique()).isEmpty();
        assertThat(Lot.builder().build().getHistorique()).isEmpty();
    }

    @Test
    void enregistrerStockage_fixeLaDateEtDonneLeStatutConformeParDefaut() {
        Lot lot = new Lot();
        LocalDateTime date = LocalDateTime.of(2026, 3, 15, 10, 0);

        lot.enregistrerStockage(date);

        assertThat(lot.getDateStockage()).isEqualTo(date);
        assertThat(lot.getStatus()).isEqualTo(StatusEnum.COMPLIANT);
    }

    @Test
    void enregistrerStockage_gardeLeStatutDemandeEtEcraseLaDateDeStockage() {
        Lot lot = Lot.builder().status(StatusEnum.BLOCKED).dateStockage(LocalDateTime.of(2020, 1, 1, 0, 0)).build();
        LocalDateTime date = LocalDateTime.of(2026, 3, 15, 10, 0);

        lot.enregistrerStockage(date);

        assertThat(lot.getStatus()).isEqualTo(StatusEnum.BLOCKED);
        assertThat(lot.getDateStockage()).isEqualTo(date);
    }

    private static Lot lotStockeLe(LocalDateTime dateStockage, StatusEnum status) {
        return Lot.builder().status(status).dateStockage(dateStockage).build();
    }

    @Test
    void estExpire_unLotStockeDepuisPlusDUnAnEstPerime() {
        Lot lot = lotStockeLe(AUJOURDHUI.minusYears(1).minusDays(1).atTime(12, 0), StatusEnum.COMPLIANT);

        assertThat(lot.estExpire(AUJOURDHUI)).isTrue();
    }

    @Test
    void estExpire_lePremierAnniversaireLeLotNEstPasEncorePerime_quelleQueSoitLHeure() {
        for (LocalTime heure : new LocalTime[]{LocalTime.MIDNIGHT, LocalTime.NOON, LocalTime.of(23, 59, 59)}) {
            Lot lot = lotStockeLe(AUJOURDHUI.minusYears(1).atTime(heure), StatusEnum.COMPLIANT);
            assertThat(lot.estExpire(AUJOURDHUI)).as(heure.toString()).isFalse();
        }
    }

    @Test
    void estExpire_unLotRecentOuSansDateDeStockageNEstPasPerime() {
        assertThat(lotStockeLe(AUJOURDHUI.minusMonths(3).atTime(9, 0), StatusEnum.COMPLIANT).estExpire(AUJOURDHUI)).isFalse();
        assertThat(lotStockeLe(null, StatusEnum.COMPLIANT).estExpire(AUJOURDHUI)).isFalse();
    }

    @Test
    void estExpire_laDateDeProductionNEntrePasEnCompte() {
        Lot produitIlYATroisAnsMaisStockeRecemment = lotStockeLe(AUJOURDHUI.minusDays(10).atTime(9, 0), StatusEnum.COMPLIANT);
        produitIlYATroisAnsMaisStockeRecemment.setDateProduction(AUJOURDHUI.minusYears(3));
        Lot produitHierMaisStockeDepuisDeuxAns = lotStockeLe(AUJOURDHUI.minusYears(2).atTime(9, 0), StatusEnum.COMPLIANT);
        produitHierMaisStockeDepuisDeuxAns.setDateProduction(AUJOURDHUI.minusDays(1));
        Lot sansDateDeProduction = lotStockeLe(AUJOURDHUI.minusYears(2).atTime(9, 0), StatusEnum.COMPLIANT);
        sansDateDeProduction.setDateProduction(null);

        assertThat(produitIlYATroisAnsMaisStockeRecemment.estExpire(AUJOURDHUI)).isFalse();
        assertThat(produitHierMaisStockeDepuisDeuxAns.estExpire(AUJOURDHUI)).isTrue();
        assertThat(sansDateDeProduction.estExpire(AUJOURDHUI)).isTrue();
    }

    @Test
    void estExpire_unLotDejaExpireNeLEstPasDeNouveau() {
        Lot lot = lotStockeLe(AUJOURDHUI.minusYears(3).atTime(9, 0), StatusEnum.EXPIRED);

        assertThat(lot.estExpire(AUJOURDHUI)).isFalse();
    }

    @Test
    void estExpire_quelqueSoitLeStatutDuLot() {
        for (StatusEnum status : new StatusEnum[]{StatusEnum.COMPLIANT, StatusEnum.ALERT, StatusEnum.BLOCKED, StatusEnum.SHIPPED}) {
            Lot lot = lotStockeLe(AUJOURDHUI.minusYears(2).atTime(9, 0), status);
            assertThat(lot.estExpire(AUJOURDHUI)).as(status.name()).isTrue();
        }
    }

    @Test
    void dateLimiteDeStockage_estIlYAUnAn() {
        assertThat(Lot.dateLimiteDeStockage(AUJOURDHUI)).isEqualTo(LocalDate.of(2025, 3, 15));
    }

    @Test
    void lesTransitionsDeStatut() {
        Lot lot = new Lot();

        lot.passerEnAlerte();
        assertThat(lot.getStatus()).isEqualTo(StatusEnum.ALERT);

        lot.expirer();
        assertThat(lot.getStatus()).isEqualTo(StatusEnum.EXPIRED);

        LocalDateTime sortie = LocalDateTime.of(2026, 3, 15, 10, 0);
        lot.expedier(sortie);
        assertThat(lot.getStatus()).isEqualTo(StatusEnum.SHIPPED);
        assertThat(lot.getDateSortie()).isEqualTo(sortie);
    }

    @Test
    void deplacerVers_changeLaZoneDuLot() {
        Lot lot = Lot.builder().zone(zone("A")).build();
        UUID destination = UUID.randomUUID();

        lot.deplacerVers(destination);

        assertThat(lot.getZone().getIdZone()).isEqualTo(destination);
    }

    @Test
    void getEntrepot_estCeluiDeLaZone() {
        Zone zone = zone("A");

        assertThat(Lot.builder().zone(zone).build().getEntrepot()).isSameAs(zone.getEntrepot());
        assertThat(new Lot().getEntrepot()).isNull();
    }
}
