package fr.epsi.mspr2.infrastructure.dto;

import java.util.List;
import java.util.UUID;

public class ReportingStockDto {
    public int totalLots;
    public double avgAgeDays;
    public double complianceRate;
    public List<UUID> lotIdsUrgentFifo;
}
