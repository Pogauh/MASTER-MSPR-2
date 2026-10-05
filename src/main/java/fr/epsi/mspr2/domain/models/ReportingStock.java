package fr.epsi.mspr2.domain.models;

import java.util.List;
import java.util.UUID;

public record ReportingStock(int totalLots, double avgAgeDays, double complianceRate, List<UUID> lotIdsUrgentFifo) {
}
