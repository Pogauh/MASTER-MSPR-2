package fr.epsi.mspr2.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CapteurSummaryResponseDTO {
    private UUID zoneId;
    private String zoneName;
    private BigDecimal lastTemp;
    private BigDecimal lastHumidity;
    private BigDecimal avgTemp24h;
    private BigDecimal avgHumidity24h;
}