package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.AlerteDetailsResponseDto;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(AlerteMapperImpl.class)
class AlerteMapperTest {

    @Autowired
    AlerteMapper mapper;

    @Test
    void toDto_leTypeEstExposeSousFormeDeTexte() {
        UUID idAlerte = UUID.randomUUID();
        UUID idZone = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.of(2026, 3, 15, 10, 0);

        AlerteDetailsResponseDto dto = mapper.toDto(new AlerteDetails(idAlerte, TypeAlerteEnum.BASSEHUMIDITE, "sec", date, idZone));

        assertThat(dto.idAlerte).isEqualTo(idAlerte);
        assertThat(dto.typeAlerte).isEqualTo("BASSEHUMIDITE");
        assertThat(dto.description).isEqualTo("sec");
        assertThat(dto.dateEmission).isEqualTo(date);
        assertThat(dto.idZone).isEqualTo(idZone);
    }

    @Test
    void toDto_nul() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
