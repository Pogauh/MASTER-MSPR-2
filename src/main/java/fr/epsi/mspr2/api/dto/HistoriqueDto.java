package fr.epsi.mspr2.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class HistoriqueDto {

    public UUID idHistorique;
    public String type;
    public LocalDateTime dateEmission;
    public UUID zonePrecedente;
    public UUID zoneActuelle;
    public String detail;
}
