package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoriqueServiceTest {

    @Mock
    HistoriquePort historiquePort;
    @InjectMocks
    HistoriqueService service;

    @Test
    void findAllHistoriquesForLot() {
        UUID idLot = UUID.randomUUID();
        List<Historique> historiques = List.of(new Historique());
        when(historiquePort.findByLotId(idLot)).thenReturn(historiques);

        assertThat(service.findAllHistoriquesForLot(idLot)).isEqualTo(historiques);
    }

    @Test
    void findAllHistoriquesForLotByType() {
        UUID idLot = UUID.randomUUID();
        List<Historique> historiques = List.of(new Historique());
        when(historiquePort.findByLotIdAndType(idLot, TypeHistoriqueEnum.ALERTE)).thenReturn(historiques);

        assertThat(service.findAllHistoriquesForLotByType(idLot, TypeHistoriqueEnum.ALERTE)).isEqualTo(historiques);
    }
}
