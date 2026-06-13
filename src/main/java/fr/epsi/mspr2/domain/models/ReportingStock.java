package fr.epsi.mspr2.domain.models;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReportingStock {
    public int totalLots;
    public double avgAgeDays;
    public double complianceRate;
    public List<UUID> lotIdsUrgentFifo;
}
