package fr.epsi.mspr2.infrastructure.dto;

import java.util.UUID;

public class MesureStatusResponseDTO {
    public UUID idMesure;
    public String status;

    public MesureStatusResponseDTO() {
    }

    public MesureStatusResponseDTO(UUID idMesure, String status) {
        this.idMesure = idMesure;
        this.status = status;
    }
}

