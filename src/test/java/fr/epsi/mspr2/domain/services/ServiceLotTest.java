package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.adapters.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.ports.LotPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceLotTest {
    private LotPort lotPort;
    private ServiceLot serviceLot;

    @BeforeEach
    void setUp() {
        lotPort = mock(LotPort.class);
        serviceLot = new ServiceLot(lotPort);
    }

    @Test
    void createLot_shouldReturnStatusDTOWithGeneratedId() {
        UUID generatedId = UUID.randomUUID();
        doAnswer(invocation -> {
            Lot saved = invocation.getArgument(0);
            saved.setIdLot(generatedId);
            return null;
        }).when(lotPort).save(any(Lot.class));

        Lot lot = new Lot();
        LotStatusResponseDTO response = serviceLot.createLot(lot);

        assertNotNull(response);
        assertEquals(generatedId, response.idLot);
        assertNull(response.status);
        verify(lotPort).save(any(Lot.class));
    }

    @Test
    void deleteLot_shouldReturnStatusDTOAndDeleteEntity() {
        UUID id = UUID.randomUUID();
        Lot lot = new Lot();
        lot.setIdLot(id);
        when(lotPort.findById(id)).thenReturn(lot);

        LotStatusResponseDTO response = serviceLot.deleteLot(id);

        assertNotNull(response);
        assertEquals(id, response.idLot);
        verify(lotPort).delete(id);
    }

    @Test
    void deleteLot_shouldReturnNullWhenEntityMissing() {
        UUID id = UUID.randomUUID();
        when(lotPort.findById(id)).thenReturn(null);

        LotStatusResponseDTO response = serviceLot.deleteLot(id);

        assertNull(response);
        verify(lotPort, never()).delete(any());
    }
}

