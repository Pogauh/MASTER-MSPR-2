package fr.epsi.mspr2.adapters.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class LotRequestDTO {
    public String status;
    public Integer quantite;
    public Integer caracteristique;
    public LocalDateTime dateSortie;
    public LocalDateTime dateStockage;
    public UUID idExploitation;
}

