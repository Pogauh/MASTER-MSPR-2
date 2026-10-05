package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.MesureLotResponseDTO;
import fr.epsi.mspr2.api.dto.MesureRequestDTO;
import fr.epsi.mspr2.api.dto.MesureResponseDTO;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.NouvelleMesure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(MesureMapperImpl.class)
class MesureMapperTest {

    @Autowired
    MesureMapper mapper;

    private Mesure mesure(UUID idMesure, UUID idCapteur) {
        return Mesure.builder().idMesure(idMesure).temperature(new BigDecimal("29.38")).humidite(new BigDecimal("54.18"))
                .dateMesure(LocalDateTime.of(2026, 3, 15, 15, 42, 44))
                .capteur(idCapteur == null ? null : Capteur.builder().idCapteur(idCapteur).build()).build();
    }

    @Test
    void toResponse_exposeLIdentifiantDuCapteur() {
        UUID idMesure = UUID.randomUUID();
        UUID idCapteur = UUID.randomUUID();

        MesureResponseDTO dto = mapper.toResponse(mesure(idMesure, idCapteur));

        assertThat(dto.idMesure).isEqualTo(idMesure);
        assertThat(dto.temperature).isEqualByComparingTo("29.38");
        assertThat(dto.humidite).isEqualByComparingTo("54.18");
        assertThat(dto.dateMesure).isEqualTo(LocalDateTime.of(2026, 3, 15, 15, 42, 44));
        assertThat(dto.idCapteur).isEqualTo(idCapteur);
    }

    @Test
    void toResponse_sansCapteur() {
        assertThat(mapper.toResponse(mesure(UUID.randomUUID(), null)).idCapteur).isNull();
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toLotResponse_neGardeQueLesValeursEtLaDate() {
        MesureLotResponseDTO dto = mapper.toLotResponse(mesure(UUID.randomUUID(), UUID.randomUUID()));

        assertThat(dto.temperature).isEqualByComparingTo("29.38");
        assertThat(dto.humidite).isEqualByComparingTo("54.18");
        assertThat(dto.dateMesure).isEqualTo(LocalDateTime.of(2026, 3, 15, 15, 42, 44));
    }

    @Test
    void toNouvelleMesure_reprendLaRequete() {
        UUID idCapteur = UUID.randomUUID();
        MesureRequestDTO request = new MesureRequestDTO();
        request.temperature = new BigDecimal("27.50");
        request.humidite = new BigDecimal("56.20");
        request.dateMesure = LocalDateTime.of(2026, 3, 15, 16, 0);
        request.idCapteur = idCapteur;

        assertThat(mapper.toNouvelleMesure(request)).isEqualTo(
                new NouvelleMesure(new BigDecimal("27.50"), new BigDecimal("56.20"), LocalDateTime.of(2026, 3, 15, 16, 0), idCapteur));
    }

    @Test
    void toNouvelleMesure_supporteUneRequeteVide() {
        assertThat(mapper.toNouvelleMesure(new MesureRequestDTO())).isEqualTo(new NouvelleMesure(null, null, null, null));
    }
}
