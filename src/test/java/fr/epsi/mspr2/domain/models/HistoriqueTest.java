package fr.epsi.mspr2.domain.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static fr.epsi.mspr2.domain.support.Fixtures.MAINTENANT;
import static fr.epsi.mspr2.domain.support.Fixtures.lot;
import static fr.epsi.mspr2.domain.support.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;

class HistoriqueTest {

    @Test
    void transfert_decritLeDeplacementEntreDeuxZones() {
        Lot lot = lot(zone("A"));
        UUID destination = UUID.randomUUID();

        Historique historique = Historique.transfert(lot, destination, MAINTENANT);

        assertThat(historique.getType()).isEqualTo(TypeHistoriqueEnum.TRANSFERT);
        assertThat(historique.getDateEmission()).isEqualTo(MAINTENANT);
        assertThat(historique.getZonePrecedente()).isEqualTo(lot.getZone().getIdZone());
        assertThat(historique.getZoneActuelle()).isEqualTo(destination);
        assertThat(historique.getIdLot()).isEqualTo(lot.getIdLot());
        assertThat(historique.getIdHistorique()).isNull();
        assertThat(historique.getDetail()).isEqualTo(
                "Transfert du lot " + lot.getIdLot() + " de la zone " + lot.getZone().getIdZone() + " vers la zone " + destination);
    }

    @Test
    void alerte_laisseLeLotDansSaZone() {
        Lot lot = lot(zone("A"));

        Historique historique = Historique.alerte(lot, "Lot expiré", MAINTENANT);

        assertThat(historique.getType()).isEqualTo(TypeHistoriqueEnum.ALERTE);
        assertThat(historique.getZonePrecedente()).isEqualTo(lot.getZone().getIdZone());
        assertThat(historique.getZoneActuelle()).isEqualTo(lot.getZone().getIdZone());
        assertThat(historique.getDetail()).isEqualTo("Lot expiré");
        assertThat(historique.getIdLot()).isEqualTo(lot.getIdLot());
        assertThat(historique.getDateEmission()).isEqualTo(MAINTENANT);
    }

    @Test
    void expedition_nIndiquePasDeZone() {
        UUID idLot = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.of(2026, 3, 15, 10, 0);

        Historique historique = Historique.expedition(idLot, date);

        assertThat(historique.getType()).isEqualTo(TypeHistoriqueEnum.SHIPPED);
        assertThat(historique.getDetail()).isEqualTo("Le lot a été expédié.");
        assertThat(historique.getZonePrecedente()).isNull();
        assertThat(historique.getZoneActuelle()).isNull();
        assertThat(historique.getIdLot()).isEqualTo(idLot);
        assertThat(historique.getDateEmission()).isEqualTo(date);
    }
}
