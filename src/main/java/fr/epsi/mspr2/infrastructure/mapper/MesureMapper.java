package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureStatusResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class MesureMapper {
    public MesureResponseDTO toResponse(Mesure mesure) {
        MesureResponseDTO dto = new MesureResponseDTO();
        dto.idMesure = mesure.getIdMesure();
        dto.temperature = mesure.getTemperature();
        dto.humidite = mesure.getHumidite();
        dto.dateMesure = mesure.getDateMesure();
        dto.idCapteur = mesure.getCapteur() != null ? mesure.getCapteur().getIdCapteur() : null;
        return dto;
    }

    public MesureStatusResponseDTO toStatusResponse(Mesure mesure) {
        MesureStatusResponseDTO dto = new MesureStatusResponseDTO();
        dto.idMesure = mesure.getIdMesure();
        return dto;
    }
}
