package fr.epsi.mspr2.dto;

import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MesureMapperTest {
    @Test
    void toResponse_shouldMapFields() {
        Mesure mesure = new Mesure();
        UUID id = UUID.randomUUID();
        mesure.setIdMesure(id);
        mesure.setTemperature(new BigDecimal("23.00"));
        mesure.setHumidite(new BigDecimal("70.00"));
        mesure.setDateMesure(LocalDateTime.now());
        Capteur capteur = new Capteur();
        UUID capteurId = UUID.randomUUID();
        capteur.setIdCapteur(capteurId);
        mesure.setCapteur(capteur);
        MesureResponseDTO dto = MesureMapper.toResponse(mesure);
        assertEquals(id, dto.idMesure);
        assertEquals(new BigDecimal("23.00"), dto.temperature);
        assertEquals(new BigDecimal("70.00"), dto.humidite);
        assertEquals(capteurId, dto.idCapteur);
    }
}

