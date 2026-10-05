package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceFixtures;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceIT;
import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.MesureEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@PersistenceIT
class MesureAdapterIT {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 15, 10, 0);

    @Autowired
    TestEntityManager em;
    @Autowired
    MesureAdapter adapter;

    PersistenceFixtures fixtures;
    ZoneEntity zoneA;
    ZoneEntity zoneB;
    CapteurEntity capteurA;
    CapteurEntity capteurB;

    @BeforeEach
    void setUp() {
        fixtures = new PersistenceFixtures(em);
        var entrepot = fixtures.entrepot("Principal");
        zoneA = fixtures.zone(entrepot, "A");
        zoneB = fixtures.zone(entrepot, "B");
        capteurA = fixtures.capteur(zoneA);
        capteurB = fixtures.capteur(zoneB);
    }

    private MesureEntity mesure(CapteurEntity capteur, String temperature, LocalDateTime date) {
        return fixtures.mesure(capteur, temperature, "55.00", date);
    }

    @Test
    void save_enregistreLaMesureEtRenvoieSonEtatPersistant() {
        Mesure nouvelle = Mesure.builder()
                .temperature(new BigDecimal("29.38"))
                .humidite(new BigDecimal("54.18"))
                .dateMesure(T0)
                .capteur(Capteur.builder().idCapteur(capteurA.getIdCapteur()).build())
                .build();

        Mesure enregistree = adapter.save(nouvelle);
        fixtures.flushAndClear();

        assertThat(enregistree.getIdMesure()).isNotNull();
        assertThat(enregistree.getCapteur().getIdCapteur()).isEqualTo(capteurA.getIdCapteur());
        assertThat(enregistree.getTemperature()).isEqualByComparingTo("29.38");
        MesureEntity relue = em.find(MesureEntity.class, enregistree.getIdMesure());
        assertThat(relue.getTemperature()).isEqualByComparingTo("29.38");
        assertThat(relue.getHumidite()).isEqualByComparingTo("54.18");
        assertThat(relue.getDateMesure()).isEqualTo(T0);
        assertThat(relue.getCapteur().getIdCapteur()).isEqualTo(capteurA.getIdCapteur());
    }

    @Test
    void save_garde_lEchelleDesValeursRecues() {
        Mesure nouvelle = Mesure.builder().temperature(new BigDecimal("35")).humidite(new BigDecimal("55.5")).dateMesure(T0)
                .capteur(Capteur.builder().idCapteur(capteurA.getIdCapteur()).build()).build();

        Mesure enregistree = adapter.save(nouvelle);

        // l'alerte est rédigée avec les valeurs telles que reçues (« 35 » et non « 35.00 »)
        assertThat(enregistree.getTemperature()).hasToString("35");
        assertThat(enregistree.getHumidite()).hasToString("55.5");
    }

    @Test
    void save_surUnCapteurInconnu_echoue() {
        Mesure nouvelle = Mesure.builder().temperature(new BigDecimal("29")).humidite(new BigDecimal("55")).dateMesure(T0)
                .capteur(Capteur.builder().idCapteur(UUID.randomUUID()).build()).build();

        assertThatThrownBy(() -> {
            adapter.save(nouvelle);
            fixtures.flushAndClear();
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    void findById_etDeleteById() {
        MesureEntity mesure = mesure(capteurA, "20.00", T0);
        fixtures.flushAndClear();

        Mesure trouvee = adapter.findById(mesure.getIdMesure()).orElseThrow();
        assertThat(trouvee.getTemperature()).isEqualByComparingTo("20.00");
        assertThat(trouvee.getCapteur().getIdCapteur()).isEqualTo(capteurA.getIdCapteur());
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();

        adapter.deleteById(mesure.getIdMesure());
        fixtures.flushAndClear();

        assertThat(adapter.findById(mesure.getIdMesure())).isEmpty();
    }

    @Test
    void findByCapteurId_neRenvoieQueLesMesuresDuCapteur() {
        MesureEntity premiere = mesure(capteurA, "20.00", T0);
        MesureEntity seconde = mesure(capteurA, "21.00", T0.plusMinutes(1));
        mesure(capteurB, "22.00", T0);
        fixtures.flushAndClear();

        assertThat(adapter.findByCapteurId(capteurA.getIdCapteur())).extracting(Mesure::getIdMesure)
                .containsExactlyInAnyOrder(premiere.getIdMesure(), seconde.getIdMesure());
        assertThat(adapter.findByCapteurId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findLatestByCapteurId_lesPlusRecentesEnPremierLimiteesAuNombreDemande() {
        for (int i = 0; i < 7; i++) {
            mesure(capteurA, "2" + i + ".00", T0.plusMinutes(i));
        }
        mesure(capteurB, "99.00", T0.plusHours(1));
        fixtures.flushAndClear();

        List<Mesure> dernieres = adapter.findLatestByCapteurId(capteurA.getIdCapteur(), 5);

        assertThat(dernieres).extracting(Mesure::getTemperature)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("26"), new BigDecimal("25"), new BigDecimal("24"), new BigDecimal("23"), new BigDecimal("22"));
        assertThat(adapter.findLatestByCapteurId(capteurA.getIdCapteur(), 1)).extracting(Mesure::getDateMesure)
                .containsExactly(T0.plusMinutes(6));
    }

    @Test
    void findLatestByCapteurId_ignoreLesMesuresSansDate() {
        mesure(capteurA, "20.00", T0);
        MesureEntity sansDate = mesure(capteurA, "99.00", null);
        fixtures.flushAndClear();

        assertThat(adapter.findLatestByCapteurId(capteurA.getIdCapteur(), 5)).extracting(Mesure::getIdMesure)
                .doesNotContain(sansDate.getIdMesure()).hasSize(1);
    }

    @Test
    void findByCapteurIdSince_dateIncluse() {
        mesure(capteurA, "10.00", T0.minusSeconds(1));
        MesureEntity pile = mesure(capteurA, "20.00", T0);
        MesureEntity apres = mesure(capteurA, "30.00", T0.plusHours(1));
        mesure(capteurB, "40.00", T0.plusHours(1));
        fixtures.flushAndClear();

        assertThat(adapter.findByCapteurIdSince(capteurA.getIdCapteur(), T0)).extracting(Mesure::getIdMesure)
                .containsExactlyInAnyOrder(pile.getIdMesure(), apres.getIdMesure());
    }

    @Test
    void findByZoneIdBetween_bornesIncluses_etSeulementLaZoneDemandee() {
        mesure(capteurA, "10.00", T0.minusSeconds(1));
        MesureEntity aDebut = mesure(capteurA, "20.00", T0);
        MesureEntity aFin = mesure(capteurA, "30.00", T0.plusHours(1));
        mesure(capteurA, "40.00", T0.plusHours(1).plusSeconds(1));
        mesure(capteurB, "50.00", T0.plusMinutes(5));
        fixtures.flushAndClear();

        assertThat(adapter.findByZoneIdBetween(zoneA.getIdZone(), T0, T0.plusHours(1))).extracting(Mesure::getIdMesure)
                .containsExactlyInAnyOrder(aDebut.getIdMesure(), aFin.getIdMesure());
        assertThat(adapter.findByZoneIdBetween(zoneA.getIdZone(), null, T0.plusHours(1))).isEmpty();
    }

    @Test
    void findLatestByZoneId() {
        mesure(capteurA, "10.00", T0);
        MesureEntity derniere = mesure(capteurA, "20.00", T0.plusMinutes(10));
        mesure(capteurB, "30.00", T0.plusHours(1));
        fixtures.flushAndClear();

        assertThat(adapter.findLatestByZoneId(zoneA.getIdZone())).map(Mesure::getIdMesure).contains(derniere.getIdMesure());
        assertThat(adapter.findLatestByZoneId(UUID.randomUUID())).isEmpty();
    }
}
