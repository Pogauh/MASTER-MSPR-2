package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.domain.ports.EntrepotPort;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import fr.epsi.mspr2.infrastructure.dto.ZoneResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Service
public class EntrepotService {
    private final EntrepotPort entrepotPort;
    private final CapteurPort capteurPort;

    public List<Entrepot> getAllEntrepots() {
        return entrepotPort.findAll();
    }

    public List<CapteurSummary> getCapteurSummaryByEntrepotId(UUID entrepotId) {
        if (entrepotId == null) {
            throw new IllegalArgumentException("Entrepot ID ne peut pas être null");
        }

        List<Capteur> capteurs = capteurPort.findByEntrepotId(entrepotId);
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        return capteurs.stream().map(c -> {
            List<Mesure> mesures = capteurPort.findMesuresByCapteurId(c.getIdCapteur());

            Mesure last = mesures.stream()
                .filter(m -> m.getDateMesure() != null)
                .max(Comparator.comparing(Mesure::getDateMesure))
                .orElse(null);

            List<Mesure> mesures24 = mesures.stream()
                .filter(m -> m.getDateMesure() != null && !m.getDateMesure().isBefore(last24h))
                .toList();

            BigDecimal avgTemp = average(mesures24
                    .stream()
                    .map(Mesure::getTemperature)
                    .filter(Objects::nonNull)
                    .toList()
            );
            BigDecimal avgHumi = average(mesures24
                    .stream()
                    .map(Mesure::getHumidite)
                    .filter(Objects::nonNull)
                    .toList());

            BigDecimal lastTemp = last != null ? last.getTemperature() : null;
            BigDecimal lastHumi = last != null ? last.getHumidite() : null;

            Zone zone = c.getZone();
            UUID zoneId = zone != null ? zone.getIdZone() : null;
            String zoneName = zone != null ? zone.getNom() : null;

            return new CapteurSummary(zoneId, zoneName, lastTemp, lastHumi, avgTemp, avgHumi);
        }).toList();
    }

    public List<ZoneResponse> findZonesByEntrepotId(UUID idEntrepot) {
        if (idEntrepot == null) {
            throw new IllegalArgumentException("ID de zone ne peut pas être null");
        }
        var resultat  = entrepotPort.findZoneByIdEntrepot(idEntrepot);
        return resultat;
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }
}
