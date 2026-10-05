package fr.epsi.mspr2.domain.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Test
    void estExpire_unLotProduitIlYAPlusDUnAnEstPerime() {
        Lot lot = Lot.builder().status(StatusEnum.COMPLIANT).dateProduction(AUJOURDHUI.minusYears(1).minusDays(1)).build();

        assertThat(lot.estExpire(AUJOURDHUI)).isTrue();
    }

    @Test
    void estExpire_unLotProduitIlYAExactementUnAnNEstPasEncorePerime() {
        Lot lot = Lot.builder().status(StatusEnum.COMPLIANT).dateProduction(AUJOURDHUI.minusYears(1)).build();

        assertThat(lot.estExpire(AUJOURDHUI)).isFalse();
    }

    @Test
    void estExpire_unLotRecentOuSansDateDeProductionNEstPasPerime() {
        assertThat(Lot.builder().dateProduction(AUJOURDHUI.minusMonths(3)).build().estExpire(AUJOURDHUI)).isFalse();
        assertThat(Lot.builder().dateProduction(null).build().estExpire(AUJOURDHUI)).isFalse();
    }

    @Test
    void estExpire_unLotDejaExpireNeLEstPasDeNouveau() {
        Lot lot = Lot.builder().status(StatusEnum.EXPIRED).dateProduction(AUJOURDHUI.minusYears(3)).build();

        assertThat(lot.estExpire(AUJOURDHUI)).isFalse();
    }

    @Test
    void estExpire_quelqueSoitLeStatutDuLot() {
        for (StatusEnum status : new StatusEnum[]{StatusEnum.COMPLIANT, StatusEnum.ALERT, StatusEnum.BLOCKED, StatusEnum.SHIPPED}) {
            Lot lot = Lot.builder().status(status).dateProduction(AUJOURDHUI.minusYears(2)).build();
            assertThat(lot.estExpire(AUJOURDHUI)).as(status.name()).isTrue();
        }
    }

    @Test
    void dateLimiteDeProduction_estIlYAUnAn() {
        assertThat(Lot.dateLimiteDeProduction(AUJOURDHUI)).isEqualTo(LocalDate.of(2025, 3, 15));
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
