package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.ZoneSummary;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceFixtures;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceIT;
import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.EntrepotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ExploitationEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Lecture du référentiel : entrepôts, zones, capteurs et exploitations. */
@PersistenceIT
class ReferentielAdaptersIT {

    @Autowired
    TestEntityManager em;
    @Autowired
    EntrepotAdapter entrepotAdapter;
    @Autowired
    CapteurAdapter capteurAdapter;
    @Autowired
    ExploitationAdapter exploitationAdapter;

    PersistenceFixtures fixtures;
    EntrepotEntity principal;
    EntrepotEntity secondaire;
    ZoneEntity zoneA;
    ZoneEntity zoneB;
    ZoneEntity zoneC;
    CapteurEntity capteurA;
    CapteurEntity capteurB;
    CapteurEntity capteurC;

    @BeforeEach
    void setUp() {
        fixtures = new PersistenceFixtures(em);
        principal = fixtures.entrepot("Principal");
        secondaire = fixtures.entrepot("Secondaire");
        zoneA = fixtures.zone(principal, "A");
        zoneB = fixtures.zone(principal, "B");
        zoneC = fixtures.zone(secondaire, "C");
        capteurA = fixtures.capteur(zoneA);
        capteurB = fixtures.capteur(zoneB);
        capteurC = fixtures.capteur(zoneC);
    }

    // ---------------------------------------------------------------- entrepôts
    @Test
    void entrepots_findAll() {
        fixtures.flushAndClear();

        assertThat(entrepotAdapter.findAll()).extracting(Entrepot::getNom).containsExactlyInAnyOrder("Principal", "Secondaire");
        assertThat(entrepotAdapter.findAll()).allSatisfy(e -> {
            assertThat(e.getIdEntrepot()).isNotNull();
            assertThat(e.getLocalisation()).isEqualTo("Bresil");
        });
    }

    @Test
    void zones_avecLeurCapteurEtLeNombreDeLots() {
        ExploitationEntity exploitation = fixtures.exploitation();
        LocalDateTime stockage = LocalDateTime.of(2026, 1, 1, 0, 0);
        fixtures.lot(zoneA, exploitation, StatusEnum.COMPLIANT, 1, stockage, null);
        fixtures.lot(zoneA, exploitation, StatusEnum.ALERT, 1, stockage, null);
        fixtures.lot(zoneB, exploitation, StatusEnum.COMPLIANT, 1, stockage, null);
        fixtures.lot(zoneC, exploitation, StatusEnum.COMPLIANT, 1, stockage, null);
        fixtures.flushAndClear();

        List<ZoneSummary> zones = entrepotAdapter.findZoneSummariesByEntrepotId(principal.getIdEntrepot());

        assertThat(zones).containsExactlyInAnyOrder(
                new ZoneSummary(zoneA.getIdZone(), "A", principal.getIdEntrepot(), capteurA.getIdCapteur(), 2L),
                new ZoneSummary(zoneB.getIdZone(), "B", principal.getIdEntrepot(), capteurB.getIdCapteur(), 1L));
    }

    @Test
    void zones_uneZoneVideAUnNombreDeLotsNul() {
        fixtures.flushAndClear();

        assertThat(entrepotAdapter.findZoneSummariesByEntrepotId(secondaire.getIdEntrepot())).containsExactly(
                new ZoneSummary(zoneC.getIdZone(), "C", secondaire.getIdEntrepot(), capteurC.getIdCapteur(), 0L));
    }

    @Test
    void zones_entrepotInconnu() {
        assertThat(entrepotAdapter.findZoneSummariesByEntrepotId(UUID.randomUUID())).isEmpty();
    }

    // ---------------------------------------------------------------- capteurs
    @Test
    void capteur_findById_avecSaZoneEtSonEntrepot() {
        fixtures.flushAndClear();

        Capteur capteur = capteurAdapter.findById(capteurA.getIdCapteur()).orElseThrow();

        assertThat(capteur.getCodeSerie()).isEqualTo(capteurA.getCodeSerie());
        assertThat(capteur.getZone().getIdZone()).isEqualTo(zoneA.getIdZone());
        assertThat(capteur.getZone().getNom()).isEqualTo("A");
        assertThat(capteur.getEntrepot().getIdEntrepot()).isEqualTo(principal.getIdEntrepot());
        assertThat(capteur.getEntrepot().getNom()).isEqualTo("Principal");
        assertThat(capteurAdapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void capteur_findByEntrepotId_avecLeurZone() {
        fixtures.flushAndClear();

        List<Capteur> capteurs = capteurAdapter.findByEntrepotId(principal.getIdEntrepot());

        assertThat(capteurs).extracting(Capteur::getIdCapteur).containsExactlyInAnyOrder(capteurA.getIdCapteur(), capteurB.getIdCapteur());
        assertThat(capteurs).extracting(c -> c.getZone().getNom()).containsExactlyInAnyOrder("A", "B");
        assertThat(capteurAdapter.findByEntrepotId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void capteur_findAllIds() {
        fixtures.flushAndClear();

        assertThat(capteurAdapter.findAllIds())
                .containsExactlyInAnyOrder(capteurA.getIdCapteur(), capteurB.getIdCapteur(), capteurC.getIdCapteur());
    }

    // ---------------------------------------------------------------- exploitations
    @Test
    void exploitations_findAll() {
        ExploitationEntity exploitation = fixtures.exploitation();
        fixtures.flushAndClear();

        assertThat(exploitationAdapter.findAll()).containsExactly(Exploitation.builder()
                .idExploitation(exploitation.getIdExploitation()).nom("Exploitation Flamenco").adresse("804 rue du Barista").build());
    }
}
