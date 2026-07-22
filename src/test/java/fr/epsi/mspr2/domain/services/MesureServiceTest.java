package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.MesureRequest;
import fr.epsi.mspr2.domain.ports.MesurePort;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MesureServiceTest {

    private MesurePort mesurePort;
    private AlerteService alerteService;
    private MesureMapper mesureMapper;
    private MesureService service;

    @BeforeEach
    void setUp() {
        mesurePort = mock(MesurePort.class);
        alerteService = mock(AlerteService.class);
        mesureMapper = mock(MesureMapper.class);
        service = new MesureService(mesurePort, alerteService, mesureMapper);
    }

    private MesureRequest buildRequest(UUID capteurId) {
        MesureRequest req = new MesureRequest();
        req.idCapteur = capteurId;
        req.temperature = new BigDecimal("22.0");
        req.humidite = new BigDecimal("55.0");
        return req;
    }

    @Test
    void createMesure_shouldSaveAndSendAlerteWhenValid() {
        UUID capteurId = UUID.randomUUID();
        MesureRequest req = buildRequest(capteurId);
        Mesure mesure = new Mesure();
        MesureStatusResponseDTO dto = new MesureStatusResponseDTO();
        dto.idMesure = UUID.randomUUID();

        when(mesureMapper.mesureRequestToMesureEntity(req)).thenReturn(mesure);
        when(mesureMapper.toStatusResponse(mesure)).thenReturn(dto);

        MesureStatusResponseDTO result = service.createMesure(req);

        verify(mesurePort).save(mesure);
        verify(alerteService).sendAlerte(mesure);
        assertThat(result.idMesure).isEqualTo(dto.idMesure);
    }

    @Test
    void createMesure_shouldThrowWhenIdCapteurIsNull() {
        MesureRequest req = buildRequest(null);

        assertThatThrownBy(() -> service.createMesure(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idCapteur est obligatoire");

        verifyNoInteractions(mesurePort, alerteService);
    }

    @Test
    void getMesureById_shouldReturnDtoWhenFound() {
        UUID id = UUID.randomUUID();
        Mesure mesure = new Mesure();
        mesure.setIdMesure(id);
        MesureResponseDTO dto = new MesureResponseDTO();
        dto.idMesure = id;

        when(mesurePort.findById(id)).thenReturn(mesure);
        when(mesureMapper.toResponse(mesure)).thenReturn(dto);

        MesureResponseDTO result = service.getMesureById(id);

        assertThat(result.idMesure).isEqualTo(id);
    }

    @Test
    void getMesureById_shouldReturnNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(mesurePort.findById(id)).thenReturn(null);

        MesureResponseDTO result = service.getMesureById(id);

        assertThat(result).isNull();
    }

    @Test
    void deleteMesure_shouldDeleteAndReturnDtoWhenFound() {
        UUID id = UUID.randomUUID();
        Mesure mesure = new Mesure();
        mesure.setIdMesure(id);
        MesureStatusResponseDTO dto = new MesureStatusResponseDTO();
        dto.idMesure = id;

        when(mesurePort.findById(id)).thenReturn(mesure);
        when(mesureMapper.toStatusResponse(mesure)).thenReturn(dto);

        MesureStatusResponseDTO result = service.deleteMesure(id);

        verify(mesurePort).delete(id);
        assertThat(result.idMesure).isEqualTo(id);
    }

    @Test
    void deleteMesure_shouldReturnNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(mesurePort.findById(id)).thenReturn(null);

        MesureStatusResponseDTO result = service.deleteMesure(id);

        verify(mesurePort, never()).delete(any());
        assertThat(result).isNull();
    }
}

