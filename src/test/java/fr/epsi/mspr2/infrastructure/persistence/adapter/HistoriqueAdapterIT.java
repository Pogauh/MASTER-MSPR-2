package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceFixtures;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceIT;
import fr.epsi.mspr2.infrastructure.persistence.entity.HistoriqueEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.LotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@PersistenceIT
class HistoriqueAdapterIT {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 15, 10, 0);

    @Autowired
    TestEntityManager em;
    @Autowired
    HistoriqueAdapter adapter;

    PersistenceFixtures fixtures;
    ZoneEntity zone;
    LotEntity lot;
    LotEntity autreLot;

    @BeforeEach
    void setUp() {
        fixtures = new PersistenceFixtures(em);
        zone = fixtures.zone(fixtures.entrepot("Principal"), "A");
        var exploitation = fixtures.exploitation();
        lot = fixtures.lot(zone, exploitation, StatusEnum.COMPLIANT, 10, T0.minusDays(30), null);
        autreLot = fixtures.lot(zone, exploitation, StatusEnum.COMPLIANT, 10, T0.minusDays(30), null);
    }

    @Test
    void save_enregistreLEvenementEtRenvoieSonEtatPersistant() {
        UUID zonePrecedente = UUID.randomUUID();
        UUID zoneActuelle = zone.getIdZone();
        Historique transfert = Historique.builder()
                .type(TypeHistoriqueEnum.TRANSFERT)
                .dateEmission(T0)
                .zonePrecedente(zonePrecedente)
                .zoneActuelle(zoneActuelle)
                .detail("Transfert")
                .idLot(lot.getIdLot())
                .build();

        Historique enregistre = adapter.save(transfert);
        fixtures.flushAndClear();

        assertThat(enregistre.getIdHistorique()).isNotNull();
        assertThat(enregistre.getIdLot()).isEqualTo(lot.getIdLot());
        HistoriqueEntity relu = em.find(HistoriqueEntity.class, enregistre.getIdHistorique());
        assertThat(relu.getType()).isEqualTo(TypeHistoriqueEnum.TRANSFERT);
        assertThat(relu.getDateEmission()).isEqualTo(T0);
        assertThat(relu.getZonePrecedente()).isEqualTo(zonePrecedente);
        assertThat(relu.getZoneActuelle()).isEqualTo(zoneActuelle);
        assertThat(relu.getDetail()).isEqualTo("Transfert");
        assertThat(relu.getLot().getIdLot()).isEqualTo(lot.getIdLot());
    }

    @Test
    void save_unEvenementSansZones() {
        Historique expedition = Historique.expedition(lot.getIdLot(), T0);

        Historique enregistre = adapter.save(expedition);
        fixtures.flushAndClear();

        HistoriqueEntity relu = em.find(HistoriqueEntity.class, enregistre.getIdHistorique());
        assertThat(relu.getType()).isEqualTo(TypeHistoriqueEnum.SHIPPED);
        assertThat(relu.getZonePrecedente()).isNull();
        assertThat(relu.getZoneActuelle()).isNull();
        assertThat(relu.getDetail()).isEqualTo("Le lot a été expédié.");
    }

    @Test
    void save_surUnLotInconnu_echoue() {
        Historique orphelin = Historique.expedition(UUID.randomUUID(), T0);

        assertThatThrownBy(() -> {
            adapter.save(orphelin);
            fixtures.flushAndClear();
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    void findByLotId_neRenvoieQueLesEvenementsDuLot() {
        HistoriqueEntity transfert = fixtures.historique(lot, TypeHistoriqueEnum.TRANSFERT, T0, null, zone.getIdZone());
        HistoriqueEntity alerte = fixtures.historique(lot, TypeHistoriqueEnum.ALERTE, T0.plusHours(1), zone.getIdZone(), zone.getIdZone());
        fixtures.historique(autreLot, TypeHistoriqueEnum.ALERTE, T0, zone.getIdZone(), zone.getIdZone());
        fixtures.flushAndClear();

        assertThat(adapter.findByLotId(lot.getIdLot())).extracting(Historique::getIdHistorique)
                .containsExactlyInAnyOrder(transfert.getIdHistorique(), alerte.getIdHistorique());
        assertThat(adapter.findByLotId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByLotIdAndType_filtreParType() {
        fixtures.historique(lot, TypeHistoriqueEnum.TRANSFERT, T0, null, zone.getIdZone());
        HistoriqueEntity alerte = fixtures.historique(lot, TypeHistoriqueEnum.ALERTE, T0.plusHours(1), zone.getIdZone(), zone.getIdZone());
        fixtures.historique(autreLot, TypeHistoriqueEnum.ALERTE, T0, zone.getIdZone(), zone.getIdZone());
        fixtures.flushAndClear();

        assertThat(adapter.findByLotIdAndType(lot.getIdLot(), TypeHistoriqueEnum.ALERTE)).singleElement().satisfies(h -> {
            assertThat(h.getIdHistorique()).isEqualTo(alerte.getIdHistorique());
            assertThat(h.getType()).isEqualTo(TypeHistoriqueEnum.ALERTE);
            assertThat(h.getZonePrecedente()).isEqualTo(zone.getIdZone());
            assertThat(h.getDetail()).isEqualTo("détail ALERTE");
        });
        assertThat(adapter.findByLotIdAndType(lot.getIdLot(), TypeHistoriqueEnum.SHIPPED)).isEmpty();
    }

    @Test
    void countByTypeAfter_dateExclue() {
        fixtures.historique(lot, TypeHistoriqueEnum.TRANSFERT, T0.minusSeconds(1), null, zone.getIdZone());
        fixtures.historique(lot, TypeHistoriqueEnum.TRANSFERT, T0, null, zone.getIdZone());
        fixtures.historique(lot, TypeHistoriqueEnum.TRANSFERT, T0.plusSeconds(1), null, zone.getIdZone());
        fixtures.historique(autreLot, TypeHistoriqueEnum.TRANSFERT, T0.plusDays(1), null, zone.getIdZone());
        fixtures.historique(lot, TypeHistoriqueEnum.ALERTE, T0.plusDays(1), null, zone.getIdZone());
        fixtures.flushAndClear();

        assertThat(adapter.countByTypeAfter(TypeHistoriqueEnum.TRANSFERT, T0)).isEqualTo(2);
        assertThat(adapter.countByTypeAfter(TypeHistoriqueEnum.SHIPPED, T0)).isZero();
    }
}
