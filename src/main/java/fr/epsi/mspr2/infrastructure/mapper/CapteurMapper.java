package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.CapteurSummary;
import fr.epsi.mspr2.infrastructure.dto.CapteurSummaryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CapteurMapper {

    public CapteurSummaryResponseDTO toResponse(CapteurSummary capteurSummary) {
        CapteurSummaryResponseDTO dto = new CapteurSummaryResponseDTO();
        dto.setZoneId(capteurSummary.getZoneId());
        dto.setZoneName(capteurSummary.getZoneName());
        dto.setAvgTemp24h(capteurSummary.getAvgTemp24h());
        dto.setAvgHumidity24h(capteurSummary.getAvgHumidity24h());
        dto.setLastHumidity(capteurSummary.getLastHumidity());
        dto.setLastTemp(capteurSummary.getLastTemp());
        return dto;
    }
}