package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.infrastructure.dto.AlerteDetailsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlerteMapper {

    @Mapping(source = "idAlerte", target = "idAlerte")
    @Mapping(source = "typeAlerte", target = "typeAlerte")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "dateEmission", target = "dateEmission")
    @Mapping(source = "idZone", target = "idZone")
    AlerteDetailsResponseDto toDtoResponse(AlerteDetails alerte);
}