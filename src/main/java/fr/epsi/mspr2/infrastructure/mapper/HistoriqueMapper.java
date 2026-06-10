package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.infrastructure.dto.HistoriqueDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoriqueMapper {

    HistoriqueDto toDto(Historique historique);
}