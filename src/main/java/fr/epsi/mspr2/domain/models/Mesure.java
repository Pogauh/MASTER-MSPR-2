package fr.epsi.mspr2.domain.models;

import lombok.Data;

@Data
public class Mesure {
    private String id_mesure;
    private double temperature;
    private double humidite;
    private long date_mesure;

    public Mesure(String id_mesure, double temperature, double humidite, long date_mesure) {
        this.id_mesure = id_mesure;
        this.temperature = temperature;
        this.humidite = humidite;
        this.date_mesure = date_mesure;
    }
}
