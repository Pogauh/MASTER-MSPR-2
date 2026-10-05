package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.NouvelleMesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static fr.epsi.mspr2.domain.support.Fixtures.CLOCK;
import static fr.epsi.mspr2.domain.support.Fixtures.MAINTENANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesureServiceTest {

    @Mock
    MesurePort mesurePort;
    @Mock
    AlerteService alerteService;

    MesureService service;
    final UUID idCapteur = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new MesureService(mesurePort, alerteService, CLOCK);
    }

    private NouvelleMesure nouvelleMesure(LocalDateTime date) {
        return new NouvelleMesure(new BigDecimal("29.38"), new BigDecimal("54.18"), date, idCapteur);
    }

    @Test
    void createMesure_enregistreLaMesureEtEvalueLAlerte() {
        LocalDateTime date = LocalDateTime.of(2026, 3, 15, 9, 30);
        UUID idMesure = UUID.randomUUID();
        when(mesurePort.save(any(Mesure.class))).thenAnswer(invocation -> {
            Mesure mesure = invocation.getArgument(0);
            mesure.setIdMesure(idMesure);
            return mesure;
        });

        Mesure resultat = service.createMesure(nouvelleMesure(date));

        ArgumentCaptor<Mesure> enregistree = ArgumentCaptor.forClass(Mesure.class);
        InOrder ordre = inOrder(mesurePort, alerteService);
        ordre.verify(mesurePort).save(enregistree.capture());
        ordre.verify(alerteService).evaluerMesure(resultat);
        assertThat(enregistree.getValue().getTemperature()).isEqualByComparingTo("29.38");
        assertThat(enregistree.getValue().getHumidite()).isEqualByComparingTo("54.18");
        assertThat(enregistree.getValue().getDateMesure()).isEqualTo(date);
        assertThat(enregistree.getValue().getCapteur().getIdCapteur()).isEqualTo(idCapteur);
        assertThat(resultat.getIdMesure()).isEqualTo(idMesure);
    }

    @Test
    void createMesure_sansDate_utiliseLaDateCourante() {
        when(mesurePort.save(any(Mesure.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mesure resultat = service.createMesure(nouvelleMesure(null));

        assertThat(resultat.getDateMesure()).isEqualTo(MAINTENANT);
    }

    @Test
    void createMesure_sansCapteur_estRefusee() {
        NouvelleMesure sansCapteur = new NouvelleMesure(new BigDecimal("29"), new BigDecimal("55"), null, null);

        assertThatThrownBy(() -> service.createMesure(sansCapteur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("idCapteur est obligatoire");

        verifyNoInteractions(mesurePort, alerteService);
    }

    @Test
    void createMesure_sansTemperatureOuHumidite_estRefusee() {
        NouvelleMesure sansTemperature = new NouvelleMesure(null, new BigDecimal("55"), null, idCapteur);
        NouvelleMesure sansHumidite = new NouvelleMesure(new BigDecimal("29"), null, null, idCapteur);

        assertThatThrownBy(() -> service.createMesure(sansTemperature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mesure doit contenir temperature et humidite");
        assertThatThrownBy(() -> service.createMesure(sansHumidite))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mesure doit contenir temperature et humidite");

        verifyNoInteractions(mesurePort, alerteService);
    }

    @Test
    void createMesure_laMesureResteEnregistreeMemeSiLEvaluationDeLAlerteEchoue() {
        when(mesurePort.save(any(Mesure.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IllegalStateException("capteur introuvable")).when(alerteService).evaluerMesure(any());

        assertThatThrownBy(() -> service.createMesure(nouvelleMesure(null))).isInstanceOf(IllegalStateException.class);

        verify(mesurePort).save(any(Mesure.class));
    }

    @Test
    void createMesure_neDonnePasLaMainALAlerteSiLEnregistrementEchoue() {
        when(mesurePort.save(any(Mesure.class))).thenThrow(new IllegalStateException("capteur inconnu"));

        assertThatThrownBy(() -> service.createMesure(nouvelleMesure(null))).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(alerteService);
    }

    @Test
    void getMesureById_renvoieLaMesureOuRien() {
        UUID id = UUID.randomUUID();
        Mesure mesure = Mesure.builder().idMesure(id).build();
        when(mesurePort.findById(id)).thenReturn(Optional.of(mesure));
        UUID inconnu = UUID.randomUUID();
        when(mesurePort.findById(inconnu)).thenReturn(Optional.empty());

        assertThat(service.getMesureById(id)).contains(mesure);
        assertThat(service.getMesureById(inconnu)).isEmpty();
    }

    @Test
    void deleteMesure_supprimeLaMesureExistante() {
        UUID id = UUID.randomUUID();
        Mesure mesure = Mesure.builder().idMesure(id).build();
        when(mesurePort.findById(id)).thenReturn(Optional.of(mesure));

        assertThat(service.deleteMesure(id)).contains(mesure);

        verify(mesurePort).deleteById(id);
    }

    @Test
    void deleteMesure_neSupprimeRienQuandLaMesureEstInconnue() {
        UUID id = UUID.randomUUID();
        when(mesurePort.findById(id)).thenReturn(Optional.empty());

        assertThat(service.deleteMesure(id)).isEmpty();

        verify(mesurePort, never()).deleteById(any());
    }
}
