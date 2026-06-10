package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.CapteurSummary;
import fr.epsi.mspr2.infrastructure.dto.CapteurSummaryResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapteurMapper {

    CapteurSummaryResponseDTO toResponse(CapteurSummary capteurSummary);
}