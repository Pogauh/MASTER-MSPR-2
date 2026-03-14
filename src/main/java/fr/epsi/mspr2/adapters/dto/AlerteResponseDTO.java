package fr.epsi.mspr2.adapters.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AlerteResponseDTO {
    public UUID idAlerte;
    public String typeAlerte;
    public String description;
    public LocalDateTime dateEmission;
}

