package fr.epsi.mspr2.infrastructure.dto;

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
    public String emplacement;
    public UUID idExploitation;
    public UUID idEntrepot;
    public List<AlerteResponseDTO> alertes;
}

