package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceFixtures;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceIT;
import fr.epsi.mspr2.infrastructure.persistence.entity.AlerteEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.EntrepotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@PersistenceIT
class AlerteAdapterIT {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 15, 10, 0);

    @Autowired
    TestEntityManager em;
    @Autowired
    AlerteAdapter adapter;

    PersistenceFixtures fixtures;
    EntrepotEntity entrepot;
    EntrepotEntity autreEntrepot;
    ZoneEntity zone;
    CapteurEntity capteur;
    CapteurEntity capteurDeLAutreEntrepot;

    @BeforeEach
    void setUp() {
        fixtures = new PersistenceFixtures(em);
        entrepot = fixtures.entrepot("Principal");
        autreEntrepot = fixtures.entrepot("Secondaire");
        zone = fixtures.zone(entrepot, "A");
        capteur = fixtures.capteur(zone);
        capteurDeLAutreEntrepot = fixtures.capteur(fixtures.zone(autreEntrepot, "A"));
    }

    @Test
    void save_uneAlerteDeCapteur() {
        Alerte alerte = Alerte.builder()
                .typeAlerte(TypeAlerteEnum.HAUTEHUMIDITE)
                .description("Humidite trop haute: 70.00 (ideal=55, tolerance=2)")
                .dateEmission(T0)
                .capteur(Capteur.builder().idCapteur(capteur.getIdCapteur()).build())
                .build();

        Alerte enregistree = adapter.save(alerte);
        fixtures.flushAndClear();

        assertThat(enregistree.getIdAlerte()).isNotNull();
        assertThat(enregistree.getCapteur().getIdCapteur()).isEqualTo(capteur.getIdCapteur());
        AlerteEntity relue = em.find(AlerteEntity.class, enregistree.getIdAlerte());
        assertThat(relue.getTypeAlerte()).isEqualTo(TypeAlerteEnum.HAUTEHUMIDITE);
        assertThat(relue.getDescription()).isEqualTo("Humidite trop haute: 70.00 (ideal=55, tolerance=2)");
        assertThat(relue.getDateEmission()).isEqualTo(T0);
        assertThat(relue.getCapteur().getIdCapteur()).isEqualTo(capteur.getIdCapteur());
    }

    @Test
    void save_uneAlerteSansCapteur_commeUneAlerteDExpiration() {
        Alerte alerte = Alerte.builder().typeAlerte(TypeAlerteEnum.EXPIRED).description("Le lot x a expiré.").dateEmission(T0).build();

        Alerte enregistree = adapter.save(alerte);
        fixtures.flushAndClear();

        assertThat(enregistree.getCapteur()).isNull();
        assertThat(em.find(AlerteEntity.class, enregistree.getIdAlerte()).getCapteur()).isNull();
    }

    @Test
    void findDetailsByEntrepotId_alertesDesCapteursDeLEntrepotAvecLeurZone() {
        AlerteEntity dansLEntrepot = fixtures.alerte(capteur, TypeAlerteEnum.HAUTETEMPERATURE, T0);
        fixtures.alerte(capteurDeLAutreEntrepot, TypeAlerteEnum.HAUTETEMPERATURE, T0);
        fixtures.alerte(null, TypeAlerteEnum.EXPIRED, T0);
        fixtures.flushAndClear();

        List<AlerteDetails> alertes = adapter.findDetailsByEntrepotId(entrepot.getIdEntrepot(), null, null);

        assertThat(alertes).containsExactly(new AlerteDetails(
                dansLEntrepot.getIdAlerte(), TypeAlerteEnum.HAUTETEMPERATURE, "alerte HAUTETEMPERATURE", T0, zone.getIdZone()));
        assertThat(adapter.findDetailsByEntrepotId(UUID.randomUUID(), null, null)).isEmpty();
    }

    @Test
    void findDetailsByEntrepotId_bornesFacultativesEtIncluses() {
        fixtures.alerte(capteur, TypeAlerteEnum.HAUTETEMPERATURE, T0.minusDays(1));
        AlerteEntity aDebut = fixtures.alerte(capteur, TypeAlerteEnum.BASSETEMPERATURE, T0);
        AlerteEntity aFin = fixtures.alerte(capteur, TypeAlerteEnum.HAUTEHUMIDITE, T0.plusDays(1));
        fixtures.alerte(capteur, TypeAlerteEnum.BASSEHUMIDITE, T0.plusDays(2));
        fixtures.flushAndClear();
        UUID idEntrepot = entrepot.getIdEntrepot();

        assertThat(adapter.findDetailsByEntrepotId(idEntrepot, T0, T0.plusDays(1))).extracting(AlerteDetails::idAlerte)
                .containsExactlyInAnyOrder(aDebut.getIdAlerte(), aFin.getIdAlerte());
        assertThat(adapter.findDetailsByEntrepotId(idEntrepot, T0.plusDays(1), null)).hasSize(2);
        assertThat(adapter.findDetailsByEntrepotId(idEntrepot, null, T0.minusDays(1))).hasSize(1);
        assertThat(adapter.findDetailsByEntrepotId(idEntrepot, null, null)).hasSize(4);
    }

    @Test
    void countByDateEmissionBetween_toutesLesAlertesDeLaPeriodeBornesIncluses() {
        fixtures.alerte(capteur, TypeAlerteEnum.HAUTETEMPERATURE, T0.minusSeconds(1));
        fixtures.alerte(capteur, TypeAlerteEnum.HAUTETEMPERATURE, T0);
        fixtures.alerte(null, TypeAlerteEnum.EXPIRED, T0.plusHours(1));
        fixtures.alerte(capteurDeLAutreEntrepot, TypeAlerteEnum.HAUTETEMPERATURE, T0.plusDays(1));
        fixtures.alerte(capteur, TypeAlerteEnum.HAUTETEMPERATURE, T0.plusDays(1).plusSeconds(1));
        fixtures.flushAndClear();

        assertThat(adapter.countByDateEmissionBetween(T0, T0.plusDays(1))).isEqualTo(3);
        assertThat(adapter.countByDateEmissionBetween(T0.plusYears(1), T0.plusYears(2))).isZero();
    }
}
