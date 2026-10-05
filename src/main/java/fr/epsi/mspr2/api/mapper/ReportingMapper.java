package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.ReportingGlobalDto;
import fr.epsi.mspr2.api.dto.ReportingQualityDto;
import fr.epsi.mspr2.api.dto.ReportingStockDto;
import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface ReportingMapper {

    ReportingGlobalDto toDto(ReportingGlobal reportingGlobal);

    ReportingQualityDto toDto(ReportingQuality reportingQuality);

    ReportingStockDto toDto(ReportingStock reportingStock);
}
