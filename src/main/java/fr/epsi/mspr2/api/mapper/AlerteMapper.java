package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.AlerteDetailsResponseDto;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface AlerteMapper {

    AlerteDetailsResponseDto toDto(AlerteDetails alerte);
}
