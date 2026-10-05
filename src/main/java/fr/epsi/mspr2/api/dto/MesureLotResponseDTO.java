package fr.epsi.mspr2.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MesureLotResponseDTO {
    public BigDecimal temperature;
    public BigDecimal humidite;
    public LocalDateTime dateMesure;
}
