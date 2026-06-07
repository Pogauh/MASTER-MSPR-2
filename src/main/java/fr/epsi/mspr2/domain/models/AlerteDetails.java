package fr.epsi.mspr2.domain.models;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AlerteDetails {

    private UUID idAlerte;
    private TypeAlerteEnum typeAlerte;
    private String description;
    private LocalDateTime dateEmission;
    private UUID idZone;

    public AlerteDetails(UUID idAlerte, TypeAlerteEnum typeAlerte, String description, LocalDateTime dateEmission, UUID idZone) {
        this.idAlerte = idAlerte;
        this.typeAlerte = typeAlerte;
        this.description = description;
        this.dateEmission = dateEmission;
        this.idZone = idZone;
    }
}