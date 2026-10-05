package fr.epsi.mspr2.api.dto;

import java.util.UUID;

public class HistoriqueResponseDto {
    public UUID idHistorique;
    public String status;

    public HistoriqueResponseDto() {
    }

    public HistoriqueResponseDto(UUID idHistorique, String status) {
        this.idHistorique = idHistorique;
        this.status = status;
    }
}
