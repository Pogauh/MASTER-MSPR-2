package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.domain.ports.MesurePort;
import fr.epsi.mspr2.domain.ports.NotificationPort;
import fr.epsi.mspr2.domain.support.PassThroughTransactionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static fr.epsi.mspr2.domain.support.Fixtures.SEUILS;
import static fr.epsi.mspr2.domain.support.Fixtures.capteur;
import static fr.epsi.mspr2.domain.support.Fixtures.lot;
import static fr.epsi.mspr2.domain.support.Fixtures.mesure;
import static fr.epsi.mspr2.domain.support.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlerteServiceTest {

    private static final UUID ID_ALERTE = UUID.randomUUID();

    @Mock
    AlertePort alertePort;
    @Mock
    MesurePort mesurePort;
    @Mock
    CapteurPort capteurPort;
    @Mock
    LotPort lotPort;
    @Mock
    HistoriquePort historiquePort;
    @Mock
    NotificationPort notificationPort;

    final PassThroughTransactionPort transaction = new PassThroughTransactionPort();
    final Zone zone = zone("A");
    final Capteur capteur = capteur(zone);
    AlerteService service;

    @BeforeEach
    void setUp() {
        service = new AlerteService(alertePort, mesurePort, capteurPort, lotPort, historiquePort, notificationPort,
                transaction, SEUILS, CLOCK);
    }

    private List<Mesure> cinqMesures(String temperature, String humidite) {
        return java.util.stream.IntStream.range(0, 5).mapToObj(i -> mesure(capteur, temperature, humidite)).toList();
    }

    private void alerteEnregistreeAvecIdentifiant() {
        when(alertePort.save(any(Alerte.class))).thenAnswer(invocation -> {
            Alerte alerte = invocation.getArgument(0);
            alerte.setIdAlerte(ID_ALERTE);
            return alerte;
        });
    }

    @Test
    void uneMesureDansLaPlage_neDeclencheRien() {
        service.evaluerMesure(mesure(capteur, "29", "55"));

        verifyNoInteractions(alertePort, mesurePort, capteurPort, lotPort, historiquePort, notificationPort);
        assertThat(transaction.executions()).isZero();
    }

    @Test
    void uneMesureSansCapteur_estRefusee() {
        Mesure sansCapteur = mesure(null, "35", "55");

        assertThatThrownBy(() -> service.evaluerMesure(sansCapteur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("L'id du capteur est obligatoire pour evaluer une alerte");
    }

    @Test
    void moinsDeCinqMesuresRecentes_neDeclenchentPasDAlerte() {
        Mesure horsPlage = mesure(capteur, "35", "55");
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(cinqMesures("35", "55").subList(0, 4));

        service.evaluerMesure(horsPlage);

        verify(alertePort, never()).save(any());
        verifyNoInteractions(notificationPort, lotPort, historiquePort, capteurPort);
    }

    @Test
    void uneSeuleMesureDansLaPlageParmiLesCinq_neDeclenchePasDAlerte() {
        List<Mesure> dernieres = new java.util.ArrayList<>(cinqMesures("35", "55"));
        dernieres.set(2, mesure(capteur, "29", "55"));
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(dernieres);

        service.evaluerMesure(mesure(capteur, "35", "55"));

        verify(alertePort, never()).save(any());
        verifyNoInteractions(notificationPort, lotPort, historiquePort, capteurPort);
    }

    @Test
    void cinqMesuresConsecutivesHorsPlage_leventUneAlerte() {
        Mesure horsPlage = mesure(capteur, "35.00", "55.00");
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(cinqMesures("35.00", "55.00"));
        when(capteurPort.findById(capteur.getIdCapteur())).thenReturn(Optional.of(capteur));
        when(lotPort.findByZoneId(zone.getIdZone())).thenReturn(List.of());
        alerteEnregistreeAvecIdentifiant();

        service.evaluerMesure(horsPlage);

        ArgumentCaptor<Alerte> alerte = ArgumentCaptor.forClass(Alerte.class);
        verify(alertePort).save(alerte.capture());
        assertThat(alerte.getValue().getTypeAlerte()).isEqualTo(TypeAlerteEnum.HAUTETEMPERATURE);
        assertThat(alerte.getValue().getDescription()).isEqualTo("Temperature trop haute: 35.00 (ideal=29, tolerance=3)");
        assertThat(alerte.getValue().getDateEmission()).isEqualTo(MAINTENANT);
        assertThat(alerte.getValue().getCapteur()).isSameAs(capteur);
        verify(notificationPort).notifierAlerte(alerte.getValue());
    }

    @ParameterizedTest(name = "{0} °C / {1} % : {2}")
    @CsvSource({
            "35.00, 55.00, HAUTETEMPERATURE",
            "20.00, 55.00, BASSETEMPERATURE",
            "29.00, 70.00, HAUTEHUMIDITE",
            "29.00, 40.00, BASSEHUMIDITE"
    })
    void leTypeDeLAlerteDependDeLaGrandeurHorsPlage(String temperature, String humidite, TypeAlerteEnum attendu) {
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(cinqMesures(temperature, humidite));
        when(capteurPort.findById(capteur.getIdCapteur())).thenReturn(Optional.of(capteur));
        when(lotPort.findByZoneId(zone.getIdZone())).thenReturn(List.of());
        alerteEnregistreeAvecIdentifiant();

        service.evaluerMesure(mesure(capteur, temperature, humidite));

        ArgumentCaptor<Alerte> alerte = ArgumentCaptor.forClass(Alerte.class);
        verify(alertePort).save(alerte.capture());
        assertThat(alerte.getValue().getTypeAlerte()).isEqualTo(attendu);
    }

    @Test
    void lAlerteMetLesLotsDeLaZoneEnAlerteEtEnGardeLaTrace() {
        Lot premier = lot(zone);
        Lot second = lot(zone);
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(cinqMesures("35", "55"));
        when(capteurPort.findById(capteur.getIdCapteur())).thenReturn(Optional.of(capteur));
        when(lotPort.findByZoneId(zone.getIdZone())).thenReturn(List.of(premier, second));
        alerteEnregistreeAvecIdentifiant();

        service.evaluerMesure(mesure(capteur, "35", "55"));

        assertThat(List.of(premier, second)).allSatisfy(lot -> assertThat(lot.getStatus()).isEqualTo(StatusEnum.ALERT));
        verify(lotPort).save(premier);
        verify(lotPort).save(second);
        ArgumentCaptor<Historique> historiques = ArgumentCaptor.forClass(Historique.class);
        verify(historiquePort, times(2)).save(historiques.capture());
        assertThat(historiques.getAllValues()).allSatisfy(historique -> {
            assertThat(historique.getType()).isEqualTo(TypeHistoriqueEnum.ALERTE);
            assertThat(historique.getDetail()).isEqualTo(ID_ALERTE.toString());
            assertThat(historique.getZonePrecedente()).isEqualTo(zone.getIdZone());
            assertThat(historique.getZoneActuelle()).isEqualTo(zone.getIdZone());
            assertThat(historique.getDateEmission()).isEqualTo(MAINTENANT);
        });
        assertThat(historiques.getAllValues()).extracting(Historique::getIdLot)
                .containsExactly(premier.getIdLot(), second.getIdLot());
    }

    @Test
    void lAlerteEtLeMarquageDesLotsSontEnregistresDansUneSeuleTransaction_etLeMailPartApresLeCommit() {
        Lot lot = lot(zone);
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(cinqMesures("35", "55"));
        when(capteurPort.findById(capteur.getIdCapteur())).thenReturn(Optional.of(capteur));
        when(lotPort.findByZoneId(zone.getIdZone())).thenReturn(List.of(lot));
        alerteEnregistreeAvecIdentifiant();
        doAnswer(invocation -> {
            assertThat(transaction.enCours()).as("la notification ne doit pas retenir la transaction").isFalse();
            return null;
        }).when(notificationPort).notifierAlerte(any());

        service.evaluerMesure(mesure(capteur, "35", "55"));

        assertThat(transaction.executions()).isEqualTo(1);
        InOrder ordre = inOrder(alertePort, lotPort, historiquePort, notificationPort);
        ordre.verify(alertePort).save(any(Alerte.class));
        ordre.verify(lotPort).save(lot);
        ordre.verify(historiquePort).save(any(Historique.class));
        ordre.verify(notificationPort).notifierAlerte(any(Alerte.class));
    }

    @Test
    void sansLotDansLaZone_lAlerteEstQuandMemeLevee() {
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(cinqMesures("35", "55"));
        when(capteurPort.findById(capteur.getIdCapteur())).thenReturn(Optional.of(capteur));
        when(lotPort.findByZoneId(zone.getIdZone())).thenReturn(List.of());
        alerteEnregistreeAvecIdentifiant();

        service.evaluerMesure(mesure(capteur, "35", "55"));

        verify(alertePort).save(any(Alerte.class));
        verify(notificationPort).notifierAlerte(any(Alerte.class));
        verifyNoInteractions(historiquePort);
    }

    @Test
    void uneAlerteDeLaMemeZoneTouchePlusieursLots_memeSiLeurStatutEtaitDifferent() {
        Lot expedie = lot(zone);
        expedie.setStatus(StatusEnum.SHIPPED);
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(cinqMesures("35", "55"));
        when(capteurPort.findById(capteur.getIdCapteur())).thenReturn(Optional.of(capteur));
        when(lotPort.findByZoneId(zone.getIdZone())).thenReturn(List.of(expedie));
        alerteEnregistreeAvecIdentifiant();

        service.evaluerMesure(mesure(capteur, "35", "55"));

        // comportement hérité : tous les lots de la zone passent en ALERT, quel que soit leur statut
        assertThat(expedie.getStatus()).isEqualTo(StatusEnum.ALERT);
    }

    @Test
    void uneAlerteSurUnCapteurInconnu_estUneErreur() {
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 5)).thenReturn(cinqMesures("35", "55"));
        when(capteurPort.findById(capteur.getIdCapteur())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluerMesure(mesure(capteur, "35", "55")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Capteur introuvable pour l'id " + capteur.getIdCapteur());
        verify(alertePort, never()).save(any());
        verifyNoInteractions(notificationPort);
    }

    @Test
    void lAnalyseDesMesuresRecentesNePorteQueSurLeCapteurDeLaMesure() {
        when(mesurePort.findLatestByCapteurId(eq(capteur.getIdCapteur()), anyInt())).thenReturn(List.of());

        service.evaluerMesure(mesure(capteur, "35", "55"));

        verify(mesurePort).findLatestByCapteurId(capteur.getIdCapteur(), AlerteService.NB_MESURES_CONSECUTIVES_HORS_PLAGE);
        assertThat(AlerteService.NB_MESURES_CONSECUTIVES_HORS_PLAGE).isEqualTo(5);
    }

    @Test
    void getAlerteByEntrepot_delegueAuPort() {
        UUID idEntrepot = UUID.randomUUID();
        LocalDateTime debut = MAINTENANT.minusDays(1);
        List<AlerteDetails> alertes = List.of(
                new AlerteDetails(UUID.randomUUID(), TypeAlerteEnum.HAUTETEMPERATURE, "trop chaud", MAINTENANT, UUID.randomUUID()));
        when(alertePort.findDetailsByEntrepotId(idEntrepot, debut, null)).thenReturn(alertes);

        assertThat(service.getAlerteByEntrepot(idEntrepot, debut, null)).isEqualTo(alertes);
    }
}
