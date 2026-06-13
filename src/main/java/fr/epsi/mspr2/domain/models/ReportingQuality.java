package fr.epsi.mspr2.domain.models;

import lombok.Data;

@Data
public class ReportingQuality {
    public int alertCount;
    public double complianceRate;
    public int nonComplianceCount;
}
