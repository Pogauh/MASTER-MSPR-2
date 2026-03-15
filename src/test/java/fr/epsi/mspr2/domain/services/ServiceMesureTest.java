package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.infrastructure.dto.MesureRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceMesureTest {
    private MesurePort mesurePort;
    private AlerteService alerteService;
    private MesureService serviceMesure;

    @BeforeEach
    void setUp() {
        mesurePort = mock(MesurePort.class);
        alerteService = mock(AlerteService.class);
        serviceMesure = new MesureService(mesurePort, alerteService);
    }

    @Test
    void createMesure_shouldReturnStatusDTOWithGeneratedId() {
        UUID capteurId = UUID.randomUUID();
        MesureRequestDTO dto = new MesureRequestDTO();
        dto.temperature = new BigDecimal("25.00");
        dto.humidite = new BigDecimal("60.00");
        dto.dateMesure = LocalDateTime.now();
        dto.idCapteur = capteurId;
        UUID generatedId = UUID.randomUUID();

        doAnswer(invocation -> {
            Mesure saved = invocation.getArgument(0);
            saved.setIdMesure(generatedId);
            return null;
        }).when(mesurePort).save(any(Mesure.class));

        MesureStatusResponseDTO response = serviceMesure.createMesure(dto);

        assertEquals(generatedId, response.idMesure);
        assertNull(response.status);
        verify(mesurePort).save(any(Mesure.class));
        verify(alerteService).sendAlerte(any(Mesure.class));
    }

    @Test
    void getAllMesuresForEntrepot_shouldReturnList() {
        Mesure mesure = new Mesure();
        mesure.setTemperature(new BigDecimal("22.00"));
        mesure.setHumidite(new BigDecimal("55.00"));
        mesure.setDateMesure(LocalDateTime.now());
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(UUID.randomUUID());
        mesure.setCapteur(capteur);
        when(mesurePort.findAllByEntrepot()).thenReturn(Collections.singletonList(mesure));
        List<MesureResponseDTO> result = serviceMesure.getAllMesuresForEntrepot();
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("22.00"), result.get(0).temperature);
    }

    @Test
    void getMesureById_shouldReturnDTO() {
        UUID id = UUID.randomUUID();
        Mesure mesure = new Mesure();
        mesure.setIdMesure(id);
        mesure.setTemperature(new BigDecimal("20.00"));
        mesure.setHumidite(new BigDecimal("50.00"));
        mesure.setDateMesure(LocalDateTime.now());
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(UUID.randomUUID());
        mesure.setCapteur(capteur);
        when(mesurePort.findById(id)).thenReturn(mesure);
        MesureResponseDTO dto = serviceMesure.getMesureById(id);
        assertNotNull(dto);
        assertEquals(new BigDecimal("20.00"), dto.temperature);
    }

    @Test
    void getMesureById_shouldReturnNullIfNotFound() {
        UUID id = UUID.randomUUID();
        when(mesurePort.findById(id)).thenReturn(null);
        MesureResponseDTO dto = serviceMesure.getMesureById(id);
        assertNull(dto);
    }

    @Test
    void deleteMesure_shouldReturnStatusDTOAndDeleteEntity() {
        UUID id = UUID.randomUUID();
        Mesure mesure = new Mesure();
        mesure.setIdMesure(id);
        when(mesurePort.findById(id)).thenReturn(mesure);
        MesureStatusResponseDTO response = serviceMesure.deleteMesure(id);
        assertNotNull(response);
        assertEquals(id, response.idMesure);
        verify(mesurePort).delete(id);
    }

    @Test
    void deleteMesure_shouldReturnNullWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        when(mesurePort.findById(id)).thenReturn(null);

        MesureStatusResponseDTO response = serviceMesure.deleteMesure(id);

        assertNull(response);
        verify(mesurePort, never()).delete(any());
    }
}

