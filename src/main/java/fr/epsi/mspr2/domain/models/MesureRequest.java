package fr.epsi.mspr2.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class MesureRequest {
    public BigDecimal temperature;
    public BigDecimal humidite;
    public LocalDateTime dateMesure;
    public UUID idCapteur;
}
