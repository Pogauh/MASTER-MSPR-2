package fr.epsi.mspr2.api.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

public class MesureRequestDTO {
    public BigDecimal temperature;
    public BigDecimal humidite;
    public LocalDateTime dateMesure;
    public UUID idCapteur;
}

