package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.ReportingGlobalDto;
import fr.epsi.mspr2.api.dto.ReportingQualityDto;
import fr.epsi.mspr2.api.dto.ReportingStockDto;
import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(ReportingMapperImpl.class)
class ReportingMapperTest {

    @Autowired
    ReportingMapper mapper;

    @Test
    void global() {
        ReportingGlobalDto dto = mapper.toDto(new ReportingGlobal(3, 10.5, 66.5, 2));

        assertThat(dto.totalLots).isEqualTo(3);
        assertThat(dto.avgAgeDays).isEqualTo(10.5);
        assertThat(dto.complianceRate).isEqualTo(66.5);
        assertThat(dto.countMouvementLast30Days).isEqualTo(2);
    }

    @Test
    void quality() {
        ReportingQualityDto dto = mapper.toDto(new ReportingQuality(4, 80.0, 1));

        assertThat(dto.alertCount).isEqualTo(4);
        assertThat(dto.complianceRate).isEqualTo(80.0);
        assertThat(dto.nonComplianceCount).isEqualTo(1);
    }

    @Test
    void stock() {
        UUID urgent = UUID.randomUUID();

        ReportingStockDto dto = mapper.toDto(new ReportingStock(5, 12.0, 100.0, List.of(urgent)));

        assertThat(dto.totalLots).isEqualTo(5);
        assertThat(dto.avgAgeDays).isEqualTo(12.0);
        assertThat(dto.complianceRate).isEqualTo(100.0);
        assertThat(dto.lotIdsUrgentFifo).containsExactly(urgent);
    }
}
