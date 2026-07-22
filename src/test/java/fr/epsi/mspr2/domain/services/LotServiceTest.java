package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.ports.LotPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LotServiceTest {

    private LotPort lotPort;
    private HistoriqueService historiqueService;
    private LotService service;

    @BeforeEach
    void setUp() {
        lotPort = mock(LotPort.class);
        historiqueService = mock(HistoriqueService.class);
        service = new LotService(lotPort, historiqueService);
    }

    @Test
    void createLot_shouldSaveAndReturnLot() {
        Lot lot = new Lot();
        lot.setIdLot(UUID.randomUUID());
        when(lotPort.save(lot)).thenReturn(null);

        Lot result = service.createLot(lot);

        verify(lotPort).save(lot);
        assertThat(result).isEqualTo(lot);
    }

    @Test
    void getLotById_shouldReturnLotWhenFound() {
        UUID id = UUID.randomUUID();
        Lot lot = new Lot();
        lot.setIdLot(id);
        when(lotPort.findById(id)).thenReturn(lot);

        Lot result = service.getLotById(id);

        assertThat(result).isEqualTo(lot);
    }

    @Test
    void getLotById_shouldReturnNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(lotPort.findById(id)).thenReturn(null);

        Lot result = service.getLotById(id);

        assertThat(result).isNull();
    }

    @Test
    void deleteLot_shouldDeleteAndReturnLotWhenFound() {
        UUID id = UUID.randomUUID();
        Lot lot = new Lot();
        lot.setIdLot(id);
        when(lotPort.findById(id)).thenReturn(lot);

        Lot result = service.deleteLot(id);

        verify(lotPort).delete(id);
        assertThat(result).isEqualTo(lot);
    }

    @Test
    void deleteLot_shouldReturnNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(lotPort.findById(id)).thenReturn(null);

        Lot result = service.deleteLot(id);

        verify(lotPort, never()).delete(any());
        assertThat(result).isNull();
    }

    @Test
    void getAllLotsForAllEntrepot_shouldReturnList() {
        Lot l1 = new Lot();
        Lot l2 = new Lot();
        when(lotPort.findAllLotsForAllEntrepot()).thenReturn(List.of(l1, l2));

        List<Lot> result = service.getAllLotsForAllEntrepot();

        assertThat(result).hasSize(2);
    }

    @Test
    void getQuantiteTotale_shouldSumQuantities() {
        Lot l1 = new Lot();
        l1.setQuantite(10);
        Lot l2 = new Lot();
        l2.setQuantite(30);
        when(lotPort.findAllLotsForAllEntrepot()).thenReturn(List.of(l1, l2));

        Integer total = service.getQuantiteTotale();

        assertThat(total).isEqualTo(40);
    }

    @Test
    void getQuantiteTotale_shouldReturnZeroWhenLotsIsNull() {
        when(lotPort.findAllLotsForAllEntrepot()).thenReturn(null);

        Integer total = service.getQuantiteTotale();

        assertThat(total).isZero();
    }

    @Test
    void updateEmplacementLot_shouldReturnNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(lotPort.findById(id)).thenReturn(null);

        Lot result = service.updateEmplacementLot(id, new Lot());

        assertThat(result).isNull();
        verify(historiqueService, never()).createTransfereHistoriqueForLot(any(), any());
    }

    @Test
    void updateEmplacementLot_shouldUpdateWhenFound() {
        UUID id = UUID.randomUUID();
        Lot existing = new Lot();
        existing.setIdLot(id);
        Lot request = new Lot();
        Lot updated = new Lot();
        updated.setIdLot(id);

        when(lotPort.findById(id)).thenReturn(existing);
        when(lotPort.updateEmplacementLot(existing, request)).thenReturn(updated);

        Lot result = service.updateEmplacementLot(id, request);

        verify(historiqueService).createTransfereHistoriqueForLot(existing, request);
        assertThat(result).isEqualTo(updated);
    }

    @Test
    void updateStatusLot_shouldReturnNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(lotPort.findById(id)).thenReturn(null);

        Lot result = service.updateStatusLot(id, new Lot());

        assertThat(result).isNull();
    }

    @Test
    void updateStatusLot_shouldUpdateWhenFound() {
        UUID id = UUID.randomUUID();
        Lot existing = new Lot();
        existing.setIdLot(id);
        Lot request = new Lot();
        request.setStatus(StatusEnum.SHIPPED);
        Lot updated = new Lot();
        updated.setStatus(StatusEnum.SHIPPED);

        when(lotPort.findById(id)).thenReturn(existing);
        when(lotPort.updateStatusLot(existing, request)).thenReturn(updated);

        Lot result = service.updateStatusLot(id, request);

        assertThat(result.getStatus()).isEqualTo(StatusEnum.SHIPPED);
    }

    @Test
    void getOldestLot_shouldDelegate() {
        Lot oldest = new Lot();
        when(lotPort.findOldestLot()).thenReturn(oldest);

        Lot result = service.getOldestLot();

        assertThat(result).isEqualTo(oldest);
    }

    @Test
    void stockOut_shouldDelegate() {
        UUID id = UUID.randomUUID();
        Historique historique = new Historique();
        when(lotPort.stockOut(id)).thenReturn(historique);

        Historique result = service.stockOut(id);

        assertThat(result).isEqualTo(historique);
    }

    @Test
    void getAllMesuresByLotId_shouldDelegate() {
        UUID id = UUID.randomUUID();
        List<Mesure> mesures = List.of(new Mesure());
        when(lotPort.findAllMesuresByLotId(id)).thenReturn(Optional.of(mesures));

        Optional<List<Mesure>> result = service.getAllMesuresByLotId(id);

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
    }
}

