package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LotAdapterTest {

    private JpaLotRepository jpaLotRepository;
    private JpaMesureRepository jpaMesureRepository;
    private JpaHistoriqueRepository historiqueJpaRepository;
    private LotAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaLotRepository = mock(JpaLotRepository.class);
        jpaMesureRepository = mock(JpaMesureRepository.class);
        historiqueJpaRepository = mock(JpaHistoriqueRepository.class);
        adapter = new LotAdapter(jpaLotRepository, jpaMesureRepository, historiqueJpaRepository);
    }

    private Lot buildLotWithExploitation(UUID exploitationId) {
        Lot lot = new Lot();
        if (exploitationId != null) {
            Exploitation exploitation = new Exploitation();
            exploitation.setIdExploitation(exploitationId);
            lot.setExploitation(exploitation);
        }
        Zone zone = new Zone();
        zone.setIdZone(UUID.randomUUID());
        lot.setZone(zone);
        return lot;
    }

    @Test
    void save_shouldThrowWhenExploitationIsNull() {
        Lot lot = new Lot();
        lot.setExploitation(null);

        assertThatThrownBy(() -> adapter.save(lot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exploitation est obligatoire");
    }

    @Test
    void save_shouldSaveLotWithDateStockage() {
        Lot lot = buildLotWithExploitation(UUID.randomUUID());
        when(jpaLotRepository.save(any())).thenReturn(lot);

        adapter.save(lot);

        assertThat(lot.getDateStockage()).isNotNull();
        verify(jpaLotRepository).save(lot);
    }

    @Test
    void findById_shouldReturnLotWhenFound() {
        UUID id = UUID.randomUUID();
        Lot lot = new Lot();
        lot.setIdLot(id);
        when(jpaLotRepository.findByIdWithHistorique(id)).thenReturn(Optional.of(lot));

        Lot result = adapter.findById(id);

        assertThat(result).isEqualTo(lot);
    }

    @Test
    void findById_shouldReturnNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(jpaLotRepository.findByIdWithHistorique(id)).thenReturn(Optional.empty());

        Lot result = adapter.findById(id);

        assertThat(result).isNull();
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        UUID id = UUID.randomUUID();

        adapter.delete(id);

        verify(jpaLotRepository).deleteById(id);
    }

    @Test
    void findAllLotsForAllEntrepot_shouldReturnAll() {
        Lot lot = new Lot();
        when(jpaLotRepository.findAll()).thenReturn(List.of(lot));

        List<Lot> result = adapter.findAllLotsForAllEntrepot();

        assertThat(result).hasSize(1);
    }

    @Test
    void updateEmplacementLot_shouldUpdateZoneAndSave() {
        UUID zoneId = UUID.randomUUID();
        Zone newZone = new Zone();
        newZone.setIdZone(zoneId);

        Lot lot = new Lot();
        Lot request = new Lot();
        request.setZone(newZone);

        when(jpaLotRepository.save(lot)).thenReturn(lot);

        Lot result = adapter.updateEmplacementLot(lot, request);

        assertThat(lot.getZone().getIdZone()).isEqualTo(zoneId);
        verify(jpaLotRepository).save(lot);
    }

    @Test
    void updateEmplacementLot_shouldNotUpdateZoneWhenRequestZoneIsNull() {
        Lot lot = new Lot();
        Zone originalZone = new Zone();
        originalZone.setIdZone(UUID.randomUUID());
        lot.setZone(originalZone);

        Lot request = new Lot();
        request.setZone(null);

        when(jpaLotRepository.save(lot)).thenReturn(lot);

        adapter.updateEmplacementLot(lot, request);

        assertThat(lot.getZone().getIdZone()).isEqualTo(originalZone.getIdZone());
    }

    @Test
    void updateStatusLot_shouldUpdateStatusAndSave() {
        Lot lot = new Lot();
        lot.setStatus(StatusEnum.COMPLIANT);
        Lot request = new Lot();
        request.setStatus(StatusEnum.SHIPPED);

        when(jpaLotRepository.save(lot)).thenReturn(lot);

        adapter.updateStatusLot(lot, request);

        assertThat(lot.getStatus()).isEqualTo(StatusEnum.SHIPPED);
        verify(jpaLotRepository).save(lot);
    }

    @Test
    void findAllMesuresByLotId_shouldReturnMesuresWithHistorique() {
        UUID lotId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        UUID zonePrecedente = UUID.randomUUID();

        Zone zone = new Zone();
        zone.setIdZone(zoneId);
        Lot lot = new Lot();
        lot.setIdLot(lotId);
        lot.setZone(zone);
        lot.setDateStockage(LocalDateTime.now().minusDays(10));

        Historique h = new Historique();
        h.setDateEmission(LocalDateTime.now().minusDays(5));
        h.setZonePrecedente(zonePrecedente);

        Mesure m1 = new Mesure();
        Mesure m2 = new Mesure();

        when(jpaLotRepository.findByIdWithHistorique(lotId)).thenReturn(Optional.of(lot));
        when(historiqueJpaRepository.findAllHistoriqueForLotByType(lotId, TypeHistoriqueEnum.TRANSFERT)).thenReturn(List.of(h));
        when(jpaMesureRepository.findAllByZoneIdAndDateMesureBetween(eq(zonePrecedente), any(), any())).thenReturn(Optional.of(List.of(m1)));
        when(jpaMesureRepository.findAllByZoneIdAndDateMesureBetween(eq(zoneId), any(), any())).thenReturn(Optional.of(List.of(m2)));

        Optional<List<Mesure>> result = adapter.findAllMesuresByLotId(lotId);

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
    }

    @Test
    void findAllMesuresByLotId_shouldReturnEmptyWhenNoMesures() {
        UUID lotId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        Zone zone = new Zone();
        zone.setIdZone(zoneId);
        Lot lot = new Lot();
        lot.setIdLot(lotId);
        lot.setZone(zone);
        lot.setDateStockage(LocalDateTime.now().minusDays(1));

        when(jpaLotRepository.findByIdWithHistorique(lotId)).thenReturn(Optional.of(lot));
        when(historiqueJpaRepository.findAllHistoriqueForLotByType(lotId, TypeHistoriqueEnum.TRANSFERT)).thenReturn(List.of());
        when(jpaMesureRepository.findAllByZoneIdAndDateMesureBetween(any(), any(), any())).thenReturn(Optional.empty());

        Optional<List<Mesure>> result = adapter.findAllMesuresByLotId(lotId);

        assertThat(result).isEmpty();
    }

    @Test
    void stockOut_shouldSetStatusShippedAndCreateHistorique() {
        UUID lotId = UUID.randomUUID();
        Lot lot = new Lot();
        lot.setIdLot(lotId);
        Zone zone = new Zone();
        zone.setIdZone(UUID.randomUUID());
        lot.setZone(zone);

        Historique savedHistorique = new Historique();
        savedHistorique.setIdHistorique(UUID.randomUUID());

        when(jpaLotRepository.findByIdWithHistorique(lotId)).thenReturn(Optional.of(lot));
        when(jpaLotRepository.save(lot)).thenReturn(lot);
        when(historiqueJpaRepository.save(any(Historique.class))).thenReturn(savedHistorique);

        Historique result = adapter.stockOut(lotId);

        assertThat(lot.getStatus()).isEqualTo(StatusEnum.SHIPPED);
        assertThat(lot.getDateSortie()).isNotNull();
        assertThat(result.getIdHistorique()).isEqualTo(savedHistorique.getIdHistorique());
        verify(historiqueJpaRepository).save(any(Historique.class));
    }

    @Test
    void findOldestLot_shouldDelegateToRepository() {
        Lot lot = new Lot();
        when(jpaLotRepository.findFirstByStatusOrderByDateStockageAsc(StatusEnum.COMPLIANT)).thenReturn(lot);

        Lot result = adapter.findOldestLot();

        assertThat(result).isEqualTo(lot);
    }
}

