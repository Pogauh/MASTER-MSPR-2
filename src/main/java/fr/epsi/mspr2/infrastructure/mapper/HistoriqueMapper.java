package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.infrastructure.dto.HistoriqueDto;
import fr.epsi.mspr2.infrastructure.dto.HistoriqueResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoriqueMapper {

    HistoriqueDto toDto(Historique historique);

    HistoriqueResponseDto toHistoriqueResponseDto(Historique historique);
}