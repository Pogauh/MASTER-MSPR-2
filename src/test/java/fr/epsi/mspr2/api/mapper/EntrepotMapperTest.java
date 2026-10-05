package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.CapteurSummaryResponseDTO;
import fr.epsi.mspr2.api.dto.EntrepotDto;
import fr.epsi.mspr2.api.dto.ZoneResponseDto;
import fr.epsi.mspr2.domain.models.CapteurSummary;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.ZoneSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(EntrepotMapperImpl.class)
class EntrepotMapperTest {

    @Autowired
    EntrepotMapper mapper;

    @Test
    void entrepot() {
        UUID id = UUID.randomUUID();

        EntrepotDto dto = mapper.toDto(Entrepot.builder().idEntrepot(id).nom("Principal").localisation("Bresil").build());

        assertThat(dto.idEntrepot).isEqualTo(id);
        assertThat(dto.nom).isEqualTo("Principal");
        assertThat(dto.localisation).isEqualTo("Bresil");
    }

    @Test
    void zone() {
        UUID idZone = UUID.randomUUID();
        UUID idEntrepot = UUID.randomUUID();
        UUID idCapteur = UUID.randomUUID();

        ZoneResponseDto dto = mapper.toDto(new ZoneSummary(idZone, "A", idEntrepot, idCapteur, 4L));

        assertThat(dto.idZone).isEqualTo(idZone);
        assertThat(dto.nomZone).isEqualTo("A");
        assertThat(dto.idEntrepot).isEqualTo(idEntrepot);
        assertThat(dto.idCapteur).isEqualTo(idCapteur);
        assertThat(dto.lotCount).isEqualTo(4L);
    }

    @Test
    void resumeDeCapteur() {
        UUID idZone = UUID.randomUUID();

        CapteurSummaryResponseDTO dto = mapper.toDto(new CapteurSummary(idZone, "A", new BigDecimal("25.00"),
                new BigDecimal("45.00"), new BigDecimal("22.50"), null));

        assertThat(dto.getZoneId()).isEqualTo(idZone);
        assertThat(dto.getZoneName()).isEqualTo("A");
        assertThat(dto.getLastTemp()).isEqualByComparingTo("25.00");
        assertThat(dto.getLastHumidity()).isEqualByComparingTo("45.00");
        assertThat(dto.getAvgTemp24h()).isEqualByComparingTo("22.50");
        assertThat(dto.getAvgHumidity24h()).isNull();
    }
}
