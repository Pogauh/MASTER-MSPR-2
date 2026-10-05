package fr.epsi.mspr2.domain.support;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.SeuilsClimatiques;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.Zone;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/** Objets du domaine prêts à l'emploi pour les tests. */
public final class Fixtures {

    /** 15 mars 2026, 10 h 00 : l'instant « maintenant » des tests de services. */
    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("UTC"));
    public static final LocalDateTime MAINTENANT = LocalDateTime.now(CLOCK);

    /** Température idéale 29 ± 3, humidité idéale 55 ± 2. */
    public static final SeuilsClimatiques SEUILS = new SeuilsClimatiques(
            new BigDecimal("29"), new BigDecimal("55"), new BigDecimal("3"), new BigDecimal("2"));

    private Fixtures() {
    }

    public static Entrepot entrepot() {
        return Entrepot.builder().idEntrepot(UUID.randomUUID()).nom("Entrepot Principal").localisation("Bresil").build();
    }

    public static Zone zone(String nom) {
        return Zone.builder().idZone(UUID.randomUUID()).nom(nom).entrepot(entrepot()).build();
    }

    public static Exploitation exploitation() {
        return Exploitation.builder().idExploitation(UUID.randomUUID()).nom("Exploitation Flamenco").adresse("804 rue du Barista").build();
    }

    public static Capteur capteur(Zone zone) {
        return Capteur.builder().idCapteur(UUID.randomUUID()).codeSerie("02:AA:BB:CC:DD:01").zone(zone).build();
    }

    public static Lot lot(Zone zone) {
        return Lot.builder()
                .idLot(UUID.randomUUID())
                .status(StatusEnum.COMPLIANT)
                .quantite(100)
                .caracteristique(2)
                .dateStockage(MAINTENANT.minusDays(30))
                .exploitation(exploitation())
                .zone(zone)
                .build();
    }

    public static Mesure mesure(Capteur capteur, String temperature, String humidite) {
        return Mesure.builder()
                .idMesure(UUID.randomUUID())
                .temperature(new BigDecimal(temperature))
                .humidite(new BigDecimal(humidite))
                .dateMesure(MAINTENANT)
                .capteur(capteur)
                .build();
    }
}
