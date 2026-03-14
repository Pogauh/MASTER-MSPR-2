package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.adapters.dto.MesureRequestDTO;
import fr.epsi.mspr2.adapters.dto.MesureResponseDTO;
import fr.epsi.mspr2.adapters.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.config.EntrepotOwnerProperties;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceMesureTest {
    private MesurePort mesurePort;
    private EntrepotOwnerProperties entrepotOwnerProperties;
    private ServiceMesure serviceMesure;

    @BeforeEach
    void setUp() {
        mesurePort = mock(MesurePort.class);
        entrepotOwnerProperties = mock(EntrepotOwnerProperties.class);
        serviceMesure = new ServiceMesure(mesurePort, entrepotOwnerProperties);
    }

    @Test
    void createMesure_shouldReturnStatusDTOWithGeneratedId() {
        UUID entrepotId = UUID.randomUUID();
        when(entrepotOwnerProperties.getEntrepotId()).thenReturn(entrepotId);
        MesureRequestDTO dto = new MesureRequestDTO();
        dto.temperature = 25;
        dto.humidite = 60;
        dto.dateMesure = LocalDateTime.now();
        dto.idEntrepot = entrepotId;
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
    }

    @Test
    void getAllMesuresForEntrepot_shouldReturnList() {
        Mesure mesure = new Mesure();
        mesure.setTemperature(22);
        mesure.setHumidite(55);
        mesure.setDateMesure(LocalDateTime.now());
        Entrepot entrepot = new Entrepot();
        entrepot.setIdEntrepot(UUID.randomUUID());
        mesure.setEntrepot(entrepot);
        when(mesurePort.findAllByEntrepot()).thenReturn(Collections.singletonList(mesure));
        List<MesureResponseDTO> result = serviceMesure.getAllMesuresForEntrepot();
        assertEquals(1, result.size());
        assertEquals(22, result.get(0).temperature);
    }

    @Test
    void getMesureById_shouldReturnDTO() {
        UUID id = UUID.randomUUID();
        Mesure mesure = new Mesure();
        mesure.setIdMesure(id);
        mesure.setTemperature(20);
        mesure.setHumidite(50);
        mesure.setDateMesure(LocalDateTime.now());
        Entrepot entrepot = new Entrepot();
        entrepot.setIdEntrepot(UUID.randomUUID());
        mesure.setEntrepot(entrepot);
        when(mesurePort.findById(id)).thenReturn(mesure);
        MesureResponseDTO dto = serviceMesure.getMesureById(id);
        assertNotNull(dto);
        assertEquals(20, dto.temperature);
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

