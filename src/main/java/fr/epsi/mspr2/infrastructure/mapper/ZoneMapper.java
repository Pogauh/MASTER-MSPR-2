package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.ZoneResponse;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.ZoneResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ZoneMapper {

    ZoneResponseDto toDtoResponse(ZoneResponse zoneResponse);

    @Mapping(source = "capteur.idCapteur", target = "idCapteur")
    MesureResponseDTO toMesureResponseDto(Mesure mesure);
}
