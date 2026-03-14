package fr.epsi.mspr2.adapters.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MesureResponseDTO {
    public UUID idMesure;
    public Integer temperature;
    public Integer humidite;
    public LocalDateTime dateMesure;
    public UUID idEntrepot;
}

