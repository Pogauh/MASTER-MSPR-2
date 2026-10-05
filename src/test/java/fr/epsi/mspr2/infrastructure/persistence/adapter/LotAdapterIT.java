package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceFixtures;
import fr.epsi.mspr2.infrastructure.persistence.PersistenceIT;
import fr.epsi.mspr2.infrastructure.persistence.entity.EntrepotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ExploitationEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.LotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@PersistenceIT
class LotAdapterIT {

    @Autowired
    TestEntityManager em;
    @Autowired
    LotAdapter adapter;

    PersistenceFixtures fixtures;
    EntrepotEntity entrepot;
    ZoneEntity zoneA;
    ZoneEntity zoneB;
    ExploitationEntity exploitation;

    @BeforeEach
    void setUp() {
        fixtures = new PersistenceFixtures(em);
        entrepot = fixtures.entrepot("Principal");
        zoneA = fixtures.zone(entrepot, "A");
        zoneB = fixtures.zone(entrepot, "B");
        exploitation = fixtures.exploitation();
    }

    private LotEntity lot(ZoneEntity zone, StatusEnum status, int quantite, LocalDateTime stockage, LocalDate production) {
        return fixtures.lot(zone, exploitation, status, quantite, stockage, production);
    }

    // ---------------------------------------------------------------- save
    @Test
    void save_creeUnLotEtRenvoieSonEtatPersistant() {
        Lot nouveau = Lot.builder()
                .status(StatusEnum.COMPLIANT)
                .quantite(40)
                .caracteristique(1)
                .dateStockage(LocalDateTime.of(2026, 3, 15, 10, 0))
                .dateProduction(LocalDate.of(2026, 1, 2))
                .exploitation(Exploitation.builder().idExploitation(exploitation.getIdExploitation()).build())
                .zone(Zone.builder().idZone(zoneA.getIdZone()).build())
                .build();

        Lot enregistre = adapter.save(nouveau);
        fixtures.flushAndClear();

        assertThat(enregistre.getIdLot()).isNotNull();
        assertThat(enregistre.getZone().getIdZone()).isEqualTo(zoneA.getIdZone());
        assertThat(enregistre.getExploitation().getIdExploitation()).isEqualTo(exploitation.getIdExploitation());
        LotEntity relu = em.find(LotEntity.class, enregistre.getIdLot());
        assertThat(relu.getStatus()).isEqualTo(StatusEnum.COMPLIANT);
        assertThat(relu.getQuantite()).isEqualTo(40);
        assertThat(relu.getCaracteristique()).isEqualTo(1);
        assertThat(relu.getDateStockage()).isEqualTo(LocalDateTime.of(2026, 3, 15, 10, 0));
        assertThat(relu.getDateProduction()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(relu.getDateSortie()).isNull();
        assertThat(relu.getZone().getIdZone()).isEqualTo(zoneA.getIdZone());
        assertThat(relu.getExploitation().getIdExploitation()).isEqualTo(exploitation.getIdExploitation());
    }

    @Test
    void save_metAJourUnLotExistantSansEnCreerUnAutre() {
        LotEntity existant = lot(zoneA, StatusEnum.COMPLIANT, 100, LocalDateTime.of(2025, 1, 10, 8, 0), LocalDate.of(2025, 1, 1));
        fixtures.historique(existant, TypeHistoriqueEnum.ALERTE, LocalDateTime.of(2025, 2, 1, 0, 0), zoneA.getIdZone(), zoneA.getIdZone());
        fixtures.flushAndClear();
        Lot lot = adapter.findById(existant.getIdLot()).orElseThrow();

        lot.setStatus(StatusEnum.SHIPPED);
        lot.setDateSortie(LocalDateTime.of(2026, 3, 15, 10, 0));
        lot.deplacerVers(zoneB.getIdZone());
        Lot enregistre = adapter.save(lot);
        fixtures.flushAndClear();

        assertThat(enregistre.getIdLot()).isEqualTo(existant.getIdLot());
        assertThat(enregistre.getZone().getIdZone()).isEqualTo(zoneB.getIdZone());
        assertThat(enregistre.getZone().getNom()).isEqualTo("B");
        LotEntity relu = em.find(LotEntity.class, existant.getIdLot());
        assertThat(relu.getStatus()).isEqualTo(StatusEnum.SHIPPED);
        assertThat(relu.getDateSortie()).isEqualTo(LocalDateTime.of(2026, 3, 15, 10, 0));
        assertThat(relu.getZone().getIdZone()).isEqualTo(zoneB.getIdZone());
        assertThat(relu.getQuantite()).isEqualTo(100);
        assertThat(relu.getHistorique()).hasSize(1);
        assertThat(em.getEntityManager().createQuery("select count(l) from LotEntity l", Long.class).getSingleResult()).isEqualTo(1);
    }

    @Test
    void save_surUneZoneInconnue_echoue() {
        Lot nouveau = Lot.builder()
                .status(StatusEnum.COMPLIANT)
                .exploitation(Exploitation.builder().idExploitation(exploitation.getIdExploitation()).build())
                .zone(Zone.builder().idZone(UUID.randomUUID()).build())
                .build();

        assertThatThrownBy(() -> {
            adapter.save(nouveau);
            fixtures.flushAndClear();
        }).isInstanceOf(RuntimeException.class);
    }

    // ---------------------------------------------------------------- lecture
    @Test
    void findById_renvoieLeLotAvecSaZoneSonEntrepotSonExploitationEtSonHistorique() {
        LotEntity lot = lot(zoneA, StatusEnum.ALERT, 100, LocalDateTime.of(2025, 1, 10, 8, 0), LocalDate.of(2025, 1, 1));
        fixtures.historique(lot, TypeHistoriqueEnum.TRANSFERT, LocalDateTime.of(2025, 1, 20, 10, 0), zoneB.getIdZone(), zoneA.getIdZone());
        fixtures.historique(lot, TypeHistoriqueEnum.ALERTE, LocalDateTime.of(2025, 2, 1, 10, 0), zoneA.getIdZone(), zoneA.getIdZone());
        fixtures.flushAndClear();

        Lot resultat = adapter.findById(lot.getIdLot()).orElseThrow();

        assertThat(resultat.getStatus()).isEqualTo(StatusEnum.ALERT);
        assertThat(resultat.getQuantite()).isEqualTo(100);
        assertThat(resultat.getDateStockage()).isEqualTo(LocalDateTime.of(2025, 1, 10, 8, 0));
        assertThat(resultat.getDateProduction()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(resultat.getZone().getNom()).isEqualTo("A");
        assertThat(resultat.getEntrepot().getIdEntrepot()).isEqualTo(entrepot.getIdEntrepot());
        assertThat(resultat.getEntrepot().getNom()).isEqualTo("Principal");
        assertThat(resultat.getExploitation().getNom()).isEqualTo("Exploitation Flamenco");
        assertThat(resultat.getHistorique()).hasSize(2)
                .extracting(h -> h.getType()).containsExactlyInAnyOrder(TypeHistoriqueEnum.TRANSFERT, TypeHistoriqueEnum.ALERTE);
        assertThat(resultat.getHistorique()).allSatisfy(h -> assertThat(h.getIdLot()).isEqualTo(lot.getIdLot()));
    }

    @Test
    void findById_inconnu() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_renvoieTousLesLotsAvecLeurHistorique() {
        LotEntity avecHistorique = lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        lot(zoneB, StatusEnum.COMPLIANT, 2, LocalDateTime.of(2025, 2, 1, 0, 0), null);
        fixtures.historique(avecHistorique, TypeHistoriqueEnum.TRANSFERT, LocalDateTime.of(2025, 1, 5, 0, 0), zoneB.getIdZone(), zoneA.getIdZone());
        fixtures.historique(avecHistorique, TypeHistoriqueEnum.ALERTE, LocalDateTime.of(2025, 1, 6, 0, 0), zoneA.getIdZone(), zoneA.getIdZone());
        fixtures.flushAndClear();

        List<Lot> lots = adapter.findAll();

        assertThat(lots).hasSize(2);
        assertThat(lots).filteredOn(l -> l.getIdLot().equals(avecHistorique.getIdLot())).singleElement()
                .satisfies(l -> assertThat(l.getHistorique()).hasSize(2));
        assertThat(lots).filteredOn(l -> !l.getIdLot().equals(avecHistorique.getIdLot())).singleElement()
                .satisfies(l -> assertThat(l.getHistorique()).isEmpty());
        assertThat(lots).allSatisfy(l -> assertThat(l.getEntrepot().getIdEntrepot()).isEqualTo(entrepot.getIdEntrepot()));
    }

    @Test
    void findByZoneId_neRenvoieQueLesLotsDeLaZone() {
        LotEntity dansA = lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        LotEntity aussiDansA = lot(zoneA, StatusEnum.SHIPPED, 2, LocalDateTime.of(2025, 1, 2, 0, 0), null);
        lot(zoneB, StatusEnum.COMPLIANT, 3, LocalDateTime.of(2025, 1, 3, 0, 0), null);
        fixtures.flushAndClear();

        List<Lot> lots = adapter.findByZoneId(zoneA.getIdZone());

        assertThat(lots).extracting(Lot::getIdLot).containsExactlyInAnyOrder(dansA.getIdLot(), aussiDansA.getIdLot());
        assertThat(lots).allSatisfy(l -> {
            assertThat(l.getZone().getIdZone()).isEqualTo(zoneA.getIdZone());
            assertThat(l.getExploitation().getIdExploitation()).isEqualTo(exploitation.getIdExploitation());
            assertThat(l.getHistorique()).isEmpty();
        });
    }

    @Test
    void findByDateProductionBefore_exclutLaDateLimiteEtLesLotsSansDate() {
        LotEntity vieux = lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), LocalDate.of(2025, 3, 14));
        lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), LocalDate.of(2025, 3, 15));
        lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), LocalDate.of(2026, 1, 1));
        lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        fixtures.flushAndClear();

        assertThat(adapter.findByDateProductionBefore(LocalDate.of(2025, 3, 15)))
                .extracting(Lot::getIdLot).containsExactly(vieux.getIdLot());
    }

    @Test
    void findOldestByStatus_renvoieLeLotLePlusAnciennementStockeDeCeStatut() {
        lot(zoneA, StatusEnum.ALERT, 1, LocalDateTime.of(2024, 1, 1, 0, 0), null);
        LotEntity ancien = lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 6, 1, 0, 0), null);
        fixtures.historique(ancien, TypeHistoriqueEnum.ALERTE, LocalDateTime.of(2025, 1, 5, 0, 0), zoneA.getIdZone(), zoneA.getIdZone());
        fixtures.flushAndClear();

        Optional<Lot> resultat = adapter.findOldestByStatus(StatusEnum.COMPLIANT);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getIdLot()).isEqualTo(ancien.getIdLot());
        assertThat(resultat.get().getHistorique()).hasSize(1);
        assertThat(adapter.findOldestByStatus(StatusEnum.BLOCKED)).isEmpty();
    }

    // ---------------------------------------------------------------- suppression
    @Test
    void deleteById_supprimeLeLot() {
        LotEntity lot = lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        fixtures.flushAndClear();

        adapter.deleteById(lot.getIdLot());
        fixtures.flushAndClear();

        assertThat(adapter.findById(lot.getIdLot())).isEmpty();
    }

    // ---------------------------------------------------------------- statistiques
    @Test
    void count_etCountByStatusIn() {
        lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        lot(zoneA, StatusEnum.COMPLIANT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        lot(zoneA, StatusEnum.ALERT, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        lot(zoneA, StatusEnum.EXPIRED, 1, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        fixtures.flushAndClear();

        assertThat(adapter.count()).isEqualTo(4);
        assertThat(adapter.countByStatusIn(List.of(StatusEnum.COMPLIANT))).isEqualTo(2);
        assertThat(adapter.countByStatusIn(List.of(StatusEnum.ALERT, StatusEnum.EXPIRED))).isEqualTo(2);
        assertThat(adapter.countByStatusIn(List.of(StatusEnum.SHIPPED))).isZero();
    }

    @Test
    void findAverageAgeInDays_moyenneDesJoursDepuisLeStockage() {
        LocalDateTime maintenant = LocalDateTime.now();
        lot(zoneA, StatusEnum.COMPLIANT, 1, maintenant.minusDays(10).minusHours(1), null);
        lot(zoneA, StatusEnum.COMPLIANT, 1, maintenant.minusDays(20).minusHours(1), null);
        lot(zoneA, StatusEnum.COMPLIANT, 1, null, null);
        fixtures.flushAndClear();

        OptionalDouble moyenne = adapter.findAverageAgeInDays();

        assertThat(moyenne).isPresent();
        assertThat(moyenne.getAsDouble()).isEqualTo(15.0);
    }

    @Test
    void findAverageAgeInDays_sansLot() {
        assertThat(adapter.findAverageAgeInDays()).isEmpty();
    }

    @Test
    void findIdsByDateStockageBetween_bornesIncluses() {
        LocalDateTime debut = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2025, 6, 1, 0, 0);
        LotEntity avant = lot(zoneA, StatusEnum.COMPLIANT, 1, debut.minusSeconds(1), null);
        LotEntity aDebut = lot(zoneA, StatusEnum.COMPLIANT, 1, debut, null);
        LotEntity aFin = lot(zoneA, StatusEnum.COMPLIANT, 1, fin, null);
        LotEntity apres = lot(zoneA, StatusEnum.COMPLIANT, 1, fin.plusSeconds(1), null);
        fixtures.flushAndClear();

        assertThat(adapter.findIdsByDateStockageBetween(debut, fin))
                .containsExactlyInAnyOrder(aDebut.getIdLot(), aFin.getIdLot())
                .doesNotContain(avant.getIdLot(), apres.getIdLot());
    }
}
