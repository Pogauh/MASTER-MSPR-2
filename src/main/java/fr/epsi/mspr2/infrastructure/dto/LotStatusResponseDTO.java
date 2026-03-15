package fr.epsi.mspr2.infrastructure.dto;

import java.util.UUID;

public class LotStatusResponseDTO {
    public UUID idLot;
    public String status;

    public LotStatusResponseDTO() {
    }

    public LotStatusResponseDTO(UUID idLot, String status) {
        this.idLot = idLot;
        this.status = status;
    }
}

