package fr.epsi.mspr2.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public class LotRequestDTO {
    public String status;
    public Integer quantite;
    public Integer caracteristique;
    public LocalDate dateProduction;
    public UUID idExploitation;
    public UUID idZone;
}