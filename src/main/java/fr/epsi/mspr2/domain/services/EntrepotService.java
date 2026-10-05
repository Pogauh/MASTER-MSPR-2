package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.CapteurSummary;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.domain.models.ZoneSummary;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import fr.epsi.mspr2.domain.ports.EntrepotPort;
import fr.epsi.mspr2.domain.ports.MesurePort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class EntrepotService {

    private final EntrepotPort entrepotPort;
    private final CapteurPort capteurPort;
    private final MesurePort mesurePort;
    private final Clock clock;

    public List<Entrepot> getAllEntrepots() {
        return entrepotPort.findAll();
    }

    /** Pour chaque capteur de l'entrepôt : dernière mesure et moyennes des dernières 24 heures. */
    public List<CapteurSummary> getCapteurSummaryByEntrepotId(UUID entrepotId) {
        if (entrepotId == null) {
            throw new IllegalArgumentException("Entrepot ID ne peut pas être null");
        }

        LocalDateTime depuis24h = LocalDateTime.now(clock).minusHours(24);
        return capteurPort.findByEntrepotId(entrepotId).stream()
                .map(capteur -> resumer(capteur, depuis24h))
                .toList();
    }

    public List<ZoneSummary> findZonesByEntrepotId(UUID idEntrepot) {
        if (idEntrepot == null) {
            throw new IllegalArgumentException("ID de zone ne peut pas être null");
        }
        return entrepotPort.findZoneSummariesByEntrepotId(idEntrepot);
    }

    private CapteurSummary resumer(Capteur capteur, LocalDateTime depuis24h) {
        Optional<Mesure> derniere = mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 1).stream().findFirst();
        List<Mesure> mesures24h = mesurePort.findByCapteurIdSince(capteur.getIdCapteur(), depuis24h);

        BigDecimal temperatureMoyenne = moyenne(mesures24h.stream().map(Mesure::getTemperature).filter(Objects::nonNull).toList());
        BigDecimal humiditeMoyenne = moyenne(mesures24h.stream().map(Mesure::getHumidite).filter(Objects::nonNull).toList());

        Zone zone = capteur.getZone();
        return new CapteurSummary(
                zone != null ? zone.getIdZone() : null,
                zone != null ? zone.getNom() : null,
                derniere.map(Mesure::getTemperature).orElse(null),
                derniere.map(Mesure::getHumidite).orElse(null),
                temperatureMoyenne,
                humiditeMoyenne);
    }

    private BigDecimal moyenne(List<BigDecimal> valeurs) {
        if (valeurs.isEmpty()) {
            return null;
        }
        BigDecimal somme = valeurs.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return somme.divide(BigDecimal.valueOf(valeurs.size()), 2, RoundingMode.HALF_UP);
    }
}
