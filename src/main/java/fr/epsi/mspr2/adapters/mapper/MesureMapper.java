package fr.epsi.mspr2.adapters.mapper;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.adapters.dto.MesureResponseDTO;
import fr.epsi.mspr2.adapters.dto.MesureStatusResponseDTO;

public class MesureMapper {
    public static MesureResponseDTO toResponse(Mesure mesure) {
        MesureResponseDTO dto = new MesureResponseDTO();
        dto.idMesure = mesure.getIdMesure();
        dto.temperature = mesure.getTemperature();
        dto.humidite = mesure.getHumidite();
        dto.dateMesure = mesure.getDateMesure();
        dto.idEntrepot = mesure.getEntrepot() != null ? mesure.getEntrepot().getIdEntrepot() : null;
        return dto;
    }

    public static MesureStatusResponseDTO toStatusResponse(Mesure mesure) {
        MesureStatusResponseDTO dto = new MesureStatusResponseDTO();
        dto.idMesure = mesure.getIdMesure();
        return dto;
    }
}
