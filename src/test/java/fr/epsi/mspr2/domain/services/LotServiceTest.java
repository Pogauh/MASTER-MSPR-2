package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.exceptions.LotIntrouvableException;
import fr.epsi.mspr2.domain.exceptions.TransfertLotInvalideException;
import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.domain.ports.MesurePort;
import fr.epsi.mspr2.domain.support.PassThroughTransactionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.epsi.mspr2.domain.support.Fixtures.CLOCK;
import static fr.epsi.mspr2.domain.support.Fixtures.MAINTENANT;
import static fr.epsi.mspr2.domain.support.Fixtures.lot;
import static fr.epsi.mspr2.domain.support.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @Mock
    LotPort lotPort;
    @Mock
    HistoriquePort historiquePort;
    @Mock
    MesurePort mesurePort;

    final PassThroughTransactionPort transaction = new PassThroughTransactionPort();
    LotService service;

    @BeforeEach
    void setUp() {
        service = new LotService(lotPort, historiquePort, mesurePort, transaction, CLOCK);
    }

    @Nested
    class Creation {

        private Lot lotACreer() {
            return Lot.builder()
                    .quantite(40)
                    .caracteristique(1)
                    .exploitation(Exploitation.builder().idExploitation(UUID.randomUUID()).build())
                    .zone(Zone.builder().idZone(UUID.randomUUID()).build())
                    .build();
        }

        @Test
        void createLot_enregistreLEntreeEnStock() {
            Lot lot = lotACreer();
            Lot enregistre = lot(zone("A"));
            when(lotPort.save(lot)).thenReturn(enregistre);

            Lot resultat = service.createLot(lot);

            assertThat(resultat).isSameAs(enregistre);
            assertThat(lot.getDateStockage()).isEqualTo(MAINTENANT);
            assertThat(lot.getStatus()).isEqualTo(StatusEnum.COMPLIANT);
        }

        @Test
        void createLot_gardeLeStatutDemande() {
            Lot lot = lotACreer();
            lot.setStatus(StatusEnum.BLOCKED);
            when(lotPort.save(lot)).thenReturn(lot);

            service.createLot(lot);

            assertThat(lot.getStatus()).isEqualTo(StatusEnum.BLOCKED);
        }

        @Test
        void createLot_exigeUneExploitation() {
            Lot sansExploitation = lotACreer();
            sansExploitation.setExploitation(null);
            Lot sansIdentifiantDExploitation = lotACreer();
            sansIdentifiantDExploitation.setExploitation(new Exploitation());

            assertThatThrownBy(() -> service.createLot(sansExploitation))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'id de l'exploitation est obligatoire pour enregistrer un lot");
            assertThatThrownBy(() -> service.createLot(sansIdentifiantDExploitation))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'id de l'exploitation est obligatoire pour enregistrer un lot");
            verifyNoInteractions(lotPort);
        }

        @Test
        void createLot_exigeUneZone() {
            Lot sansZone = lotACreer();
            sansZone.setZone(null);
            Lot sansIdentifiantDeZone = lotACreer();
            sansIdentifiantDeZone.setZone(new Zone());

            assertThatThrownBy(() -> service.createLot(sansZone))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'id de la zone est obligatoire pour enregistrer un lot");
            assertThatThrownBy(() -> service.createLot(sansIdentifiantDeZone))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'id de la zone est obligatoire pour enregistrer un lot");
            verifyNoInteractions(lotPort);
        }
    }

    @Nested
    class Consultation {

        @Test
        void getLotById_delegueAuPort() {
            Lot lot = lot(zone("A"));
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));

            assertThat(service.getLotById(lot.getIdLot())).contains(lot);
        }

        @Test
        void getAllLotsForAllEntrepot_trieDuPlusAncienAuPlusRecent_etRejetteLesLotsSansDateEnFin() {
            Lot recent = lot(zone("A"));
            recent.setDateStockage(MAINTENANT.minusDays(1));
            Lot ancien = lot(zone("B"));
            ancien.setDateStockage(MAINTENANT.minusDays(100));
            Lot sansDate = lot(zone("C"));
            sansDate.setDateStockage(null);
            Lot milieu = lot(zone("D"));
            milieu.setDateStockage(MAINTENANT.minusDays(10));
            when(lotPort.findAll()).thenReturn(List.of(recent, sansDate, ancien, milieu));

            assertThat(service.getAllLotsForAllEntrepot()).containsExactly(ancien, milieu, recent, sansDate);
        }

        @Test
        void getQuantiteTotale_additionneLesQuantitesDesLots() {
            Lot premier = lot(zone("A"));
            premier.setQuantite(100);
            Lot second = lot(zone("B"));
            second.setQuantite(250);
            when(lotPort.findAll()).thenReturn(List.of(premier, second));

            assertThat(service.getQuantiteTotale()).isEqualTo(350);
        }

        @Test
        void getQuantiteTotale_ignoreLesLotsSansQuantite_etVautZeroSansLot() {
            Lot sansQuantite = lot(zone("A"));
            sansQuantite.setQuantite(null);
            Lot avecQuantite = lot(zone("B"));
            avecQuantite.setQuantite(80);
            when(lotPort.findAll()).thenReturn(List.of(sansQuantite, avecQuantite)).thenReturn(List.of());

            assertThat(service.getQuantiteTotale()).isEqualTo(80);
            assertThat(service.getQuantiteTotale()).isZero();
        }

        @Test
        void getOldestLot_estLePlusAncienLotConforme() {
            Lot lot = lot(zone("A"));
            when(lotPort.findOldestByStatus(StatusEnum.COMPLIANT)).thenReturn(Optional.of(lot));

            assertThat(service.getOldestLot()).contains(lot);
        }
    }

    @Nested
    class Suppression {

        @Test
        void deleteLot_supprimeLeLotExistant() {
            Lot lot = lot(zone("A"));
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));

            assertThat(service.deleteLot(lot.getIdLot())).contains(lot);

            verify(lotPort).deleteById(lot.getIdLot());
        }

        @Test
        void deleteLot_neSupprimeRienQuandLeLotEstInconnu() {
            UUID inconnu = UUID.randomUUID();
            when(lotPort.findById(inconnu)).thenReturn(Optional.empty());

            assertThat(service.deleteLot(inconnu)).isEmpty();

            verify(lotPort, never()).deleteById(any());
        }
    }

    @Nested
    class Transfert {

        @Test
        void updateEmplacementLot_deplaceLeLotEtGardeLaTraceDuTransfert() {
            Zone origine = zone("A");
            Lot lot = lot(origine);
            UUID destination = UUID.randomUUID();
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));
            when(lotPort.save(lot)).thenReturn(lot);

            Optional<Lot> resultat = service.updateEmplacementLot(lot.getIdLot(), destination);

            assertThat(resultat).contains(lot);
            assertThat(lot.getZone().getIdZone()).isEqualTo(destination);
            ArgumentCaptor<Historique> historique = ArgumentCaptor.forClass(Historique.class);
            InOrder ordre = inOrder(historiquePort, lotPort);
            ordre.verify(historiquePort).save(historique.capture());
            ordre.verify(lotPort).save(lot);
            assertThat(historique.getValue().getType()).isEqualTo(TypeHistoriqueEnum.TRANSFERT);
            assertThat(historique.getValue().getZonePrecedente()).isEqualTo(origine.getIdZone());
            assertThat(historique.getValue().getZoneActuelle()).isEqualTo(destination);
            assertThat(historique.getValue().getIdLot()).isEqualTo(lot.getIdLot());
            assertThat(historique.getValue().getDateEmission()).isEqualTo(MAINTENANT);
            assertThat(transaction.executions()).as("historique et lot sont enregistrés ensemble").isEqualTo(1);
        }

        @Test
        void updateEmplacementLot_lotInconnu() {
            UUID inconnu = UUID.randomUUID();
            when(lotPort.findById(inconnu)).thenReturn(Optional.empty());

            assertThat(service.updateEmplacementLot(inconnu, UUID.randomUUID())).isEmpty();

            verifyNoInteractions(historiquePort);
            verify(lotPort, never()).save(any());
        }

        @Test
        void updateEmplacementLot_refuseLaZoneActuelle() {
            Lot lot = lot(zone("A"));
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));

            assertThatThrownBy(() -> service.updateEmplacementLot(lot.getIdLot(), lot.getZone().getIdZone()))
                    .isInstanceOf(TransfertLotInvalideException.class)
                    .hasMessage("Erreur de transfert : l'emplacement de destination est identique à l'emplacement actuel.");

            verifyNoInteractions(historiquePort);
            verify(lotPort, never()).save(any());
        }

        @Test
        void updateEmplacementLot_exigeUneZoneDeDestination() {
            Lot lot = lot(zone("A"));
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));

            assertThatThrownBy(() -> service.updateEmplacementLot(lot.getIdLot(), null))
                    .isInstanceOf(TransfertLotInvalideException.class)
                    .hasMessage("Erreur de transfert : l'un des emplacement est null.");

            verifyNoInteractions(historiquePort);
            verify(lotPort, never()).save(any());
        }

        @Test
        void updateEmplacementLot_refuseUnLotSansZone() {
            Lot sansZone = lot(null);
            when(lotPort.findById(sansZone.getIdLot())).thenReturn(Optional.of(sansZone));

            assertThatThrownBy(() -> service.updateEmplacementLot(sansZone.getIdLot(), UUID.randomUUID()))
                    .isInstanceOf(TransfertLotInvalideException.class)
                    .hasMessage("Erreur de transfert : l'un des emplacement est null.");
        }
    }

    @Nested
    class ChangementDeStatut {

        @Test
        void updateStatusLot_changeLeStatut() {
            Lot lot = lot(zone("A"));
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));
            when(lotPort.save(lot)).thenReturn(lot);

            assertThat(service.updateStatusLot(lot.getIdLot(), StatusEnum.BLOCKED)).contains(lot);

            assertThat(lot.getStatus()).isEqualTo(StatusEnum.BLOCKED);
        }

        @Test
        void updateStatusLot_sansNouveauStatutLeLotEstInchange() {
            Lot lot = lot(zone("A"));
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));
            when(lotPort.save(lot)).thenReturn(lot);

            assertThat(service.updateStatusLot(lot.getIdLot(), null)).contains(lot);

            assertThat(lot.getStatus()).isEqualTo(StatusEnum.COMPLIANT);
        }

        @Test
        void updateStatusLot_lotInconnu() {
            UUID inconnu = UUID.randomUUID();
            when(lotPort.findById(inconnu)).thenReturn(Optional.empty());

            assertThat(service.updateStatusLot(inconnu, StatusEnum.BLOCKED)).isEmpty();

            verify(lotPort, never()).save(any());
        }
    }

    @Nested
    class SortieDeStock {

        @Test
        void stockOut_expedieLeLotEtEnregistreLaSortie() {
            Lot lot = lot(zone("A"));
            UUID idHistorique = UUID.randomUUID();
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));
            when(historiquePort.save(any(Historique.class))).thenAnswer(invocation -> {
                Historique historique = invocation.getArgument(0);
                historique.setIdHistorique(idHistorique);
                return historique;
            });

            Historique resultat = service.stockOut(lot.getIdLot());

            assertThat(lot.getStatus()).isEqualTo(StatusEnum.SHIPPED);
            assertThat(lot.getDateSortie()).isEqualTo(MAINTENANT);
            verify(lotPort).save(lot);
            assertThat(resultat.getIdHistorique()).isEqualTo(idHistorique);
            assertThat(resultat.getType()).isEqualTo(TypeHistoriqueEnum.SHIPPED);
            assertThat(resultat.getDetail()).isEqualTo("Le lot a été expédié.");
            assertThat(resultat.getIdLot()).isEqualTo(lot.getIdLot());
            assertThat(resultat.getDateEmission()).isEqualTo(MAINTENANT);
            assertThat(transaction.executions()).isEqualTo(1);
        }

        @Test
        void stockOut_lotInconnu() {
            UUID inconnu = UUID.randomUUID();
            when(lotPort.findById(inconnu)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.stockOut(inconnu))
                    .isInstanceOf(LotIntrouvableException.class)
                    .hasMessageContaining(inconnu.toString());

            verify(lotPort, never()).save(any());
            verifyNoInteractions(historiquePort);
        }
    }

    @Nested
    class MesuresDUnLot {

        private Mesure mesure(String temperature) {
            return Mesure.builder().temperature(new java.math.BigDecimal(temperature)).build();
        }

        private Historique transfert(Zone origine, LocalDateTime date) {
            return Historique.builder().type(TypeHistoriqueEnum.TRANSFERT).zonePrecedente(origine.getIdZone()).dateEmission(date).build();
        }

        @Test
        void getAllMesuresByLotId_sansTransfert_lesMesuresDeLaZoneDepuisLeStockage() {
            Zone zone = zone("A");
            Lot lot = lot(zone);
            Mesure mesure = mesure("20");
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));
            when(historiquePort.findByLotIdAndType(lot.getIdLot(), TypeHistoriqueEnum.TRANSFERT)).thenReturn(List.of());
            when(mesurePort.findByZoneIdBetween(zone.getIdZone(), lot.getDateStockage(), MAINTENANT)).thenReturn(List.of(mesure));

            assertThat(service.getAllMesuresByLotId(lot.getIdLot())).containsExactly(mesure);
        }

        @Test
        void getAllMesuresByLotId_reconstitueLeParcoursDeZoneEnZone() {
            Zone premiere = zone("A");
            Zone deuxieme = zone("B");
            Zone actuelle = zone("C");
            Lot lot = lot(actuelle);
            LocalDateTime stockage = lot.getDateStockage();
            LocalDateTime premierTransfert = stockage.plusDays(5);
            LocalDateTime secondTransfert = stockage.plusDays(12);
            Mesure mesureA = mesure("20");
            Mesure mesureB = mesure("21");
            Mesure mesureC = mesure("22");
            when(lotPort.findById(lot.getIdLot())).thenReturn(Optional.of(lot));
            // les transferts reviennent dans le désordre : ils doivent être rejoués chronologiquement
            when(historiquePort.findByLotIdAndType(lot.getIdLot(), TypeHistoriqueEnum.TRANSFERT)).thenReturn(List.of(
                    transfert(deuxieme, secondTransfert), transfert(premiere, premierTransfert)));
            when(mesurePort.findByZoneIdBetween(premiere.getIdZone(), stockage, premierTransfert)).thenReturn(List.of(mesureA));
            when(mesurePort.findByZoneIdBetween(deuxieme.getIdZone(), premierTransfert, secondTransfert)).thenReturn(List.of(mesureB));
            when(mesurePort.findByZoneIdBetween(actuelle.getIdZone(), secondTransfert, MAINTENANT)).thenReturn(List.of(mesureC));

            assertThat(service.getAllMesuresByLotId(lot.getIdLot())).containsExactly(mesureA, mesureB, mesureC);
        }

        @Test
        void getAllMesuresByLotId_lotInconnu_aucuneMesure() {
            UUID inconnu = UUID.randomUUID();
            when(lotPort.findById(inconnu)).thenReturn(Optional.empty());

            assertThat(service.getAllMesuresByLotId(inconnu)).isEmpty();

            verifyNoInteractions(historiquePort, mesurePort);
        }
    }
}
