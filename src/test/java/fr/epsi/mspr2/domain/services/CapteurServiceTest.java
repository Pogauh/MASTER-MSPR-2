package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapteurServiceTest {

    @Mock
    MesurePort mesurePort;
    @InjectMocks
    CapteurService service;

    @Test
    void getMesuresByCapteurId() {
        UUID idCapteur = UUID.randomUUID();
        List<Mesure> mesures = List.of(new Mesure());
        when(mesurePort.findByCapteurId(idCapteur)).thenReturn(mesures);

        assertThat(service.getMesuresByCapteurId(idCapteur)).isEqualTo(mesures);
    }

    @Test
    void getMesuresByCapteurId_exigeUnIdentifiant() {
        assertThatThrownBy(() -> service.getMesuresByCapteurId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("capteurId est obligatoire");
        verifyNoInteractions(mesurePort);
    }
}
