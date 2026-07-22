package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HistoriqueServiceTest {

    private HistoriquePort historiquePort;
    private HistoriqueService service;

    @BeforeEach
    void setUp() {
        historiquePort = mock(HistoriquePort.class);
        service = new HistoriqueService(historiquePort);
    }

    @Test
    void createTransfereHistoriqueForLot_shouldDelegateToPort() {
        Lot lot = new Lot();
        Lot request = new Lot();

        service.createTransfereHistoriqueForLot(lot, request);

        verify(historiquePort).transfert(lot, request);
    }

    @Test
    void findAllHistoriquesForLot_shouldReturnList() {
        UUID id = UUID.randomUUID();
        Historique h = new Historique();
        when(historiquePort.findAllHistoriqueForLot(id)).thenReturn(List.of(h));

        List<Historique> result = service.findAllHistoriquesForLot(id);

        assertThat(result).hasSize(1);
    }

    @Test
    void findAllHistoriquesForLotByType_shouldReturnFilteredList() {
        UUID id = UUID.randomUUID();
        Historique h = new Historique();
        h.setType(TypeHistoriqueEnum.TRANSFERT);
        when(historiquePort.findAllHistoriqueForLotByType(id, TypeHistoriqueEnum.TRANSFERT)).thenReturn(List.of(h));

        List<Historique> result = service.findAllHistoriquesForLotByType(id, TypeHistoriqueEnum.TRANSFERT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(TypeHistoriqueEnum.TRANSFERT);
    }
}

