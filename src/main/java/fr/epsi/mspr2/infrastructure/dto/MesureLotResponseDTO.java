package fr.epsi.mspr2.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class MesureLotResponseDTO {
    public BigDecimal temperature;
    public BigDecimal humidite;
    public LocalDateTime dateMesure;
}

