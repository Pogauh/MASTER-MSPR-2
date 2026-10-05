package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.LotRequestDTO;
import fr.epsi.mspr2.api.dto.LotResponseDTO;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.models.Zone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig({LotMapperImpl.class, HistoriqueMapperImpl.class})
class LotMapperTest {

    @Autowired
    LotMapper mapper;

    @Test
    void toResponse_reprendTousLesChampsDuLot() {
        UUID idLot = UUID.randomUUID();
        UUID idZone = UUID.randomUUID();
        UUID idEntrepot = UUID.randomUUID();
        UUID idExploitation = UUID.randomUUID();
        UUID idHistorique = UUID.randomUUID();
        Lot lot = Lot.builder()
                .idLot(idLot)
                .status(StatusEnum.ALERT)
                .quantite(10)
                .caracteristique(3)
                .dateSortie(LocalDateTime.of(2026, 1, 2, 3, 4))
                .dateStockage(LocalDateTime.of(2025, 5, 6, 7, 8))
                .dateProduction(LocalDate.of(2025, 4, 1))
                .exploitation(Exploitation.builder().idExploitation(idExploitation).build())
                .zone(Zone.builder().idZone(idZone).entrepot(Entrepot.builder().idEntrepot(idEntrepot).build()).build())
                .historique(List.of(Historique.builder().idHistorique(idHistorique).type(TypeHistoriqueEnum.ALERTE).detail("d").build()))
                .build();

        LotResponseDTO dto = mapper.toResponse(lot);

        assertThat(dto.idLot).isEqualTo(idLot);
        assertThat(dto.status).isEqualTo("ALERT");
        assertThat(dto.quantite).isEqualTo(10);
        assertThat(dto.caracteristique).isEqualTo(3);
        assertThat(dto.dateSortie).isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4));
        assertThat(dto.dateStockage).isEqualTo(LocalDateTime.of(2025, 5, 6, 7, 8));
        assertThat(dto.dateProduction).isEqualTo(LocalDate.of(2025, 4, 1));
        assertThat(dto.emplacement).isEqualTo(idZone);
        assertThat(dto.idExploitation).isEqualTo(idExploitation);
        assertThat(dto.idEntrepot).isEqualTo(idEntrepot);
        assertThat(dto.historique).singleElement().satisfies(historique -> {
            assertThat(historique.idHistorique).isEqualTo(idHistorique);
            assertThat(historique.type).isEqualTo("ALERTE");
            assertThat(historique.detail).isEqualTo("d");
        });
    }

    @Test
    void toResponse_supporteUnLotIncomplet() {
        LotResponseDTO dto = mapper.toResponse(new Lot());

        assertThat(dto.idLot).isNull();
        assertThat(dto.status).isNull();
        assertThat(dto.emplacement).isNull();
        assertThat(dto.idExploitation).isNull();
        assertThat(dto.idEntrepot).isNull();
        assertThat(dto.historique).isEmpty();
    }

    @Test
    void toResponse_unLotSansEntrepotOuNul() {
        Lot lot = Lot.builder().zone(Zone.builder().idZone(UUID.randomUUID()).build()).build();

        assertThat(mapper.toResponse(lot).idEntrepot).isNull();
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toLot_designeLExploitationEtLaZoneParLeurIdentifiant() {
        UUID idExploitation = UUID.randomUUID();
        UUID idZone = UUID.randomUUID();
        LotRequestDTO request = new LotRequestDTO();
        request.status = "BLOCKED";
        request.quantite = 40;
        request.caracteristique = 1;
        request.dateProduction = LocalDate.of(2026, 1, 2);
        request.idExploitation = idExploitation;
        request.idZone = idZone;

        Lot lot = mapper.toLot(request);

        assertThat(lot.getStatus()).isEqualTo(StatusEnum.BLOCKED);
        assertThat(lot.getQuantite()).isEqualTo(40);
        assertThat(lot.getCaracteristique()).isEqualTo(1);
        assertThat(lot.getDateProduction()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(lot.getExploitation().getIdExploitation()).isEqualTo(idExploitation);
        assertThat(lot.getZone().getIdZone()).isEqualTo(idZone);
    }

    @Test
    void toLot_neReprendPasLesChampsQuiSontCalculesParLeServeur() {
        Lot lot = mapper.toLot(new LotRequestDTO());

        assertThat(lot.getIdLot()).isNull();
        assertThat(lot.getDateSortie()).isNull();
        assertThat(lot.getDateStockage()).isNull();
        assertThat(lot.getHistorique()).isEmpty();
        assertThat(lot.getStatus()).isNull();
        assertThat(lot.getExploitation()).isNull();
        assertThat(lot.getZone()).isNull();
    }

    @Test
    void toLot_unStatutInconnuEstRefuse() {
        LotRequestDTO request = new LotRequestDTO();
        request.status = "PERIME";

        assertThatThrownBy(() -> mapper.toLot(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toStatus() {
        assertThat(mapper.toStatus("COMPLIANT")).isEqualTo(StatusEnum.COMPLIANT);
        assertThat(mapper.toStatus(null)).isNull();
        assertThatThrownBy(() -> mapper.toStatus("compliant")).isInstanceOf(IllegalArgumentException.class);
    }
}
