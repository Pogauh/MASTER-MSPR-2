package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.HistoriqueDto;
import fr.epsi.mspr2.domain.models.Historique;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface HistoriqueMapper {

    HistoriqueDto toDto(Historique historique);
}
