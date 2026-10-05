package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.CapteurSummaryResponseDTO;
import fr.epsi.mspr2.api.dto.EntrepotDto;
import fr.epsi.mspr2.api.dto.ZoneResponseDto;
import fr.epsi.mspr2.domain.models.CapteurSummary;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.ZoneSummary;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface EntrepotMapper {

    EntrepotDto toDto(Entrepot entrepot);

    ZoneResponseDto toDto(ZoneSummary zone);

    CapteurSummaryResponseDTO toDto(CapteurSummary capteurSummary);
}
