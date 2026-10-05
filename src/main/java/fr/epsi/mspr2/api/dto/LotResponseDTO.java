package fr.epsi.mspr2.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LotResponseDTO {
    public UUID idLot;
    public String status;
    public Integer quantite;
    public Integer caracteristique;
    public LocalDateTime dateSortie;
    public LocalDateTime dateStockage;
    public LocalDate dateProduction;
    public UUID emplacement;
    public UUID idExploitation;
    public UUID idEntrepot;
    public List<HistoriqueDto> historique;
}
