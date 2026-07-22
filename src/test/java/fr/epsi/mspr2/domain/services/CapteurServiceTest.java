package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CapteurServiceTest {

    private CapteurPort capteurPort;
    private CapteurService service;

    @BeforeEach
    void setUp() {
        capteurPort = mock(CapteurPort.class);
        service = new CapteurService(capteurPort);
    }

    @Test
    void getMesuresByCapteurId_shouldReturnMesuresWhenValid() {
        UUID id = UUID.randomUUID();
        Mesure m = new Mesure();
        when(capteurPort.findMesuresByCapteurId(id)).thenReturn(List.of(m));

        List<Mesure> result = service.getMesuresByCapteurId(id);

        assertThat(result).hasSize(1);
    }

    @Test
    void getMesuresByCapteurId_shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> service.getMesuresByCapteurId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capteurId est obligatoire");

        verifyNoInteractions(capteurPort);
    }

    @Test
    void getMesuresByCapteurId_shouldReturnEmptyListWhenNoMesures() {
        UUID id = UUID.randomUUID();
        when(capteurPort.findMesuresByCapteurId(id)).thenReturn(List.of());

        List<Mesure> result = service.getMesuresByCapteurId(id);

        assertThat(result).isEmpty();
    }
}

