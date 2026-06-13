package fr.epsi.mspr2.domain.models;

import lombok.Data;

@Data
public class ReportingGlobal {
    public int totalLots;
    public double avgAgeDays;
    public double complianceRate;
    public int countMouvementLast30Days;
}
