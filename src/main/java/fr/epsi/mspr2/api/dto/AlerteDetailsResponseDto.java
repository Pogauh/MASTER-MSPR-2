package fr.epsi.mspr2.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AlerteDetailsResponseDto {
    public UUID idAlerte;
    public String typeAlerte;
    public String description;
    public LocalDateTime dateEmission;
    public UUID idZone;

    public AlerteDetailsResponseDto(UUID idAlerte, String typeAlerte, String description, LocalDateTime dateEmission, UUID idZone) {
        this.idAlerte = idAlerte;
        this.typeAlerte = typeAlerte;
        this.description = description;
        this.dateEmission = dateEmission;
        this.idZone = idZone;
    }
}