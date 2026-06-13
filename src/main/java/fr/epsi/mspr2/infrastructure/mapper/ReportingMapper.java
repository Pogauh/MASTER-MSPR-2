package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.infrastructure.dto.ReportingGlobalDto;
import fr.epsi.mspr2.infrastructure.dto.ReportingQualityDto;
import fr.epsi.mspr2.infrastructure.dto.ReportingStockDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportingMapper {

    ReportingGlobalDto toDto(ReportingGlobal reportingGlobal);

    ReportingQualityDto toReportingQualityDto(ReportingQuality reportingQuality);

    ReportingStockDto toReportingStockDto(ReportingStock reportingStock);
}
