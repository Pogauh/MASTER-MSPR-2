package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.HistoriqueDto;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(HistoriqueMapperImpl.class)
class HistoriqueMapperTest {

    @Autowired
    HistoriqueMapper mapper;

    @Test
    void toDto_leTypeEstExposeSousFormeDeTexte() {
        UUID idHistorique = UUID.randomUUID();
        UUID zoneA = UUID.randomUUID();
        UUID zoneB = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.of(2026, 3, 15, 10, 0);

        HistoriqueDto dto = mapper.toDto(Historique.builder().idHistorique(idHistorique).type(TypeHistoriqueEnum.SHIPPED)
                .dateEmission(date).zonePrecedente(zoneA).zoneActuelle(zoneB).detail("expédié").idLot(UUID.randomUUID()).build());

        assertThat(dto.idHistorique).isEqualTo(idHistorique);
        assertThat(dto.type).isEqualTo("SHIPPED");
        assertThat(dto.dateEmission).isEqualTo(date);
        assertThat(dto.zonePrecedente).isEqualTo(zoneA);
        assertThat(dto.zoneActuelle).isEqualTo(zoneB);
        assertThat(dto.detail).isEqualTo("expédié");
    }

    @Test
    void toDto_sansType() {
        assertThat(mapper.toDto(new Historique()).type).isNull();
    }
}
