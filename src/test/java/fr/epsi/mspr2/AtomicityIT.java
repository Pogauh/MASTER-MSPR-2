package fr.epsi.mspr2;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.NouvelleMesure;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.services.LotService;
import fr.epsi.mspr2.domain.services.MesureService;
import fr.epsi.mspr2.infrastructure.persistence.adapter.HistoriqueAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Vérifie, sur une vraie base, que les traitements composés de plusieurs écritures sont atomiques :
 * si une écriture échoue, aucune des autres ne subsiste et aucun e-mail n'est envoyé.
 */
@Import(AtomicityIT.FaultInjection.class)
class AtomicityIT extends IntegrationTestBase {

    /** Historique dont l'enregistrement peut être rendu défaillant, pour simuler une panne en cours de traitement. */
    static class FaultyHistoriquePort implements HistoriquePort {

        private final HistoriquePort delegate;
        private volatile boolean failOnSave;

        FaultyHistoriquePort(HistoriquePort delegate) {
            this.delegate = delegate;
        }

        void failOnSave(boolean failOnSave) {
            this.failOnSave = failOnSave;
        }

        @Override
        public Historique save(Historique historique) {
            if (failOnSave) {
                throw new IllegalStateException("panne simulée de la base d'historique");
            }
            return delegate.save(historique);
        }

        @Override
        public List<Historique> findByLotId(UUID idLot) {
            return delegate.findByLotId(idLot);
        }

        @Override
        public List<Historique> findByLotIdAndType(UUID idLot, TypeHistoriqueEnum type) {
            return delegate.findByLotIdAndType(idLot, type);
        }

        @Override
        public long countByTypeAfter(TypeHistoriqueEnum type, LocalDateTime date) {
            return delegate.countByTypeAfter(type, date);
        }
    }

    @TestConfiguration
    static class FaultInjection {

        @Bean
        @Primary
        FaultyHistoriquePort faultyHistoriquePort(HistoriqueAdapter adapter) {
            return new FaultyHistoriquePort(adapter);
        }
    }

    @Autowired
    FaultyHistoriquePort historiquePort;
    @Autowired
    MesureService mesureService;
    @Autowired
    LotService lotService;

    final UUID zoneA = UUID.randomUUID();
    final UUID zoneB = UUID.randomUUID();
    final UUID capteur = UUID.randomUUID();
    final UUID lot = UUID.randomUUID();

    @BeforeEach
    void seed() {
        viderLaBase();
        historiquePort.failOnSave(false);
        UUID entrepot = UUID.randomUUID();
        UUID exploitation = UUID.randomUUID();
        jdbc.update("insert into entrepot (id_entrepot, localisation, nom) values (?, ?, ?)", entrepot, "Bresil", "Principal");
        jdbc.update("insert into exploitation (id_exploitation, nom, adresse) values (?, ?, ?)", exploitation, "Flamenco", "804 rue du Barista");
        jdbc.update("insert into zone (id_zone, nom, id_entrepot) values (?, ?, ?)", zoneA, "A", entrepot);
        jdbc.update("insert into zone (id_zone, nom, id_entrepot) values (?, ?, ?)", zoneB, "B", entrepot);
        jdbc.update("insert into capteur (id_capteur, code_serie, id_zone) values (?, ?, ?)", capteur, "02:AA:BB:CC:DD:01", zoneA);
        jdbc.update("""
                insert into lot (id_lot, status, quantite, date_stockage, id_exploitation, id_zone)
                values (?, 'COMPLIANT', 10, ?, ?, ?)""", lot, Timestamp.valueOf(LocalDateTime.now().minusDays(3)), exploitation, zoneA);
    }

    private void envoyerMesureHorsPlage(int minute) {
        mesureService.createMesure(new NouvelleMesure(new BigDecimal("35"), new BigDecimal("55"),
                LocalDateTime.of(2026, 3, 15, 10, minute), capteur));
    }

    @Test
    void uneAlerteEstEnregistreeEnUnSeulBloc_etLeMailNePartQuApresLeCommit() {
        historiquePort.failOnSave(true);
        for (int minute = 0; minute < 4; minute++) {
            envoyerMesureHorsPlage(minute);
        }

        // la cinquième mesure lève l'alerte ; l'écriture de l'historique du lot tombe en panne
        assertThatThrownBy(() -> envoyerMesureHorsPlage(4)).isInstanceOf(IllegalStateException.class);

        assertThat(jdbc.queryForObject("select count(*) from alerte", Integer.class)).as("l'alerte est annulée").isZero();
        assertThat(jdbc.queryForObject("select status from lot where id_lot = ?", String.class, lot)).as("le lot reste conforme").isEqualTo("COMPLIANT");
        assertThat(jdbc.queryForObject("select count(*) from historique", Integer.class)).isZero();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        // la mesure, elle, avait déjà été enregistrée : l'échec de l'alerte ne la fait pas disparaître
        assertThat(jdbc.queryForObject("select count(*) from mesure", Integer.class)).isEqualTo(5);
    }

    @Test
    void sansPanne_laMemeSequenceLeveBienUneAlerte() {
        for (int minute = 0; minute < 5; minute++) {
            envoyerMesureHorsPlage(minute);
        }

        assertThat(jdbc.queryForObject("select count(*) from alerte", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select status from lot where id_lot = ?", String.class, lot)).isEqualTo("ALERT");
        assertThat(jdbc.queryForObject("select count(*) from historique", Integer.class)).isEqualTo(1);
    }

    @Test
    void unTransfertVersUneZoneInconnue_nEnregistreRienDuTout() {
        assertThatThrownBy(() -> lotService.updateEmplacementLot(lot, UUID.randomUUID())).isInstanceOf(RuntimeException.class);

        assertThat(jdbc.queryForObject("select count(*) from historique", Integer.class)).as("pas de transfert fantôme").isZero();
        assertThat(jdbc.queryForObject("select id_zone from lot where id_lot = ?", UUID.class, lot)).isEqualTo(zoneA);
    }

    @Test
    void unTransfertValide_enregistreLHistoriqueEtLeNouvelEmplacement() {
        lotService.updateEmplacementLot(lot, zoneB);

        assertThat(jdbc.queryForObject("select count(*) from historique where type = 'TRANSFERT'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select id_zone from lot where id_lot = ?", UUID.class, lot)).isEqualTo(zoneB);
    }

    @Test
    void uneSortieDeStockEchouee_nExpediePasLeLot() {
        historiquePort.failOnSave(true);

        assertThatThrownBy(() -> lotService.stockOut(lot)).isInstanceOf(IllegalStateException.class);

        assertThat(jdbc.queryForObject("select status from lot where id_lot = ?", String.class, lot)).isEqualTo("COMPLIANT");
        assertThat(jdbc.queryForObject("select date_sortie from lot where id_lot = ?", Timestamp.class, lot)).isNull();
    }
}
