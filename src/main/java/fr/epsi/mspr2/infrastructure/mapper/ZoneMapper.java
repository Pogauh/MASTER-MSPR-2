package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.ZoneResponse;
import fr.epsi.mspr2.infrastructure.dto.ZoneResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ZoneMapper {

    ZoneResponseDto toDtoResponse(ZoneResponse zoneResponse);
}
