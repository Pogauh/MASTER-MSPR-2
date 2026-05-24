package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceCapteurTest {

    private CapteurPort capteurPort;
    private CapteurService serviceCapteur;

    @BeforeEach
    void setUp() {
        capteurPort = mock(CapteurPort.class);
        serviceCapteur = new CapteurService(capteurPort);
    }

    @Test
    void getMesuresByCapteurId_shouldThrowWhenCapteurIdMissing() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> serviceCapteur.getMesuresByCapteurId(null)
        );

        assertEquals("capteurId est obligatoire", ex.getMessage());
    }

    @Test
    void getMesuresByCapteurIdAndHourRange_shouldCallPort() {
        UUID capteurId = UUID.randomUUID();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        when(capteurPort.findMesuresByCapteurIdAndHourRange(capteurId, startTime, endTime)).thenReturn(List.of(buildMesure()));

        List<MesureResponseDTO> result = serviceCapteur.getMesuresByCapteurIdAndHourRange(capteurId, startTime, endTime);

        assertEquals(1, result.size());
        verify(capteurPort).findMesuresByCapteurIdAndHourRange(capteurId, startTime, endTime);
    }

    private Mesure buildMesure() {
        Mesure mesure = new Mesure();
        mesure.setIdMesure(UUID.randomUUID());
        mesure.setTemperature(new BigDecimal("25.00"));
        mesure.setHumidite(new BigDecimal("60.00"));
        mesure.setDateMesure(LocalDateTime.now());

        Capteur capteur = new Capteur();
        capteur.setIdCapteur(UUID.randomUUID());
        mesure.setCapteur(capteur);
        return mesure;
    }
}