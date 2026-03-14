package fr.epsi.mspr2.adapters.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MesureRequestDTO {
    public Integer temperature;
    public Integer humidite;
    public LocalDateTime dateMesure;
    public UUID idEntrepot;
}

