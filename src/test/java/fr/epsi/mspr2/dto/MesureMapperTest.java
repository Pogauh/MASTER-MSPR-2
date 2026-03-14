package fr.epsi.mspr2.dto;

import fr.epsi.mspr2.adapters.dto.MesureResponseDTO;
import fr.epsi.mspr2.adapters.mapper.MesureMapper;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.Entrepot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MesureMapperTest {
    @Test
    void toResponse_shouldMapFields() {
        Mesure mesure = new Mesure();
        UUID id = UUID.randomUUID();
        mesure.setIdMesure(id);
        mesure.setTemperature(23);
        mesure.setHumidite(70);
        mesure.setDateMesure(LocalDateTime.now());
        Entrepot entrepot = new Entrepot();
        UUID entrepotId = UUID.randomUUID();
        entrepot.setIdEntrepot(entrepotId);
        mesure.setEntrepot(entrepot);
        MesureResponseDTO dto = MesureMapper.toResponse(mesure);
        assertEquals(id, dto.idMesure);
        assertEquals(23, dto.temperature);
        assertEquals(70, dto.humidite);
        assertEquals(entrepotId, dto.idEntrepot);
    }
}

