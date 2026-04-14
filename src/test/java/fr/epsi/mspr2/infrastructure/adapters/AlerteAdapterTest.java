package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.GenereAlerte;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.infrastructure.jpa.JpaAlerteRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaCapteurRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaGenereAlerteRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlerteAdapterTest {

    private JpaAlerteRepository jpaAlerteRepository;
    private JpaCapteurRepository jpaCapteurRepository;
    private JpaLotRepository jpaLotRepository;
    private JpaGenereAlerteRepository jpaGenereAlerteRepository;
    private AlertePort alertePort;

    @BeforeEach
    void setUp() {
        jpaAlerteRepository = mock(JpaAlerteRepository.class);
        jpaCapteurRepository = mock(JpaCapteurRepository.class);
        jpaLotRepository = mock(JpaLotRepository.class);
        jpaGenereAlerteRepository = mock(JpaGenereAlerteRepository.class);
        alertePort = new AlerteAdapter(jpaAlerteRepository, jpaCapteurRepository, jpaLotRepository, jpaGenereAlerteRepository);

        when(jpaAlerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> {
            Alerte a = invocation.getArgument(0);
            a.setIdAlerte(UUID.randomUUID());
            return a;
        });
    }

    @Test
    void isCompatible_shouldReturnTrueWhenTemperatureOutOfRange() {
        Mesure mesure = buildMesure(new BigDecimal("35.00"), new BigDecimal("55.00"));
        when(jpaCapteurRepository.findByIdWithEntrepotAndPays(mesure.getCapteur().getIdCapteur()))
            .thenReturn(Optional.of(buildCapteur()));

        boolean result = alertePort.isCompatible(mesure);

        assertTrue(result);
    }

    @Test
    void isCompatible_shouldReturnFalseWhenMesureInsideRange() {
        Mesure mesure = buildMesure(new BigDecimal("29.00"), new BigDecimal("55.00"));
        when(jpaCapteurRepository.findByIdWithEntrepotAndPays(mesure.getCapteur().getIdCapteur()))
            .thenReturn(Optional.of(buildCapteur()));

        boolean result = alertePort.isCompatible(mesure);

        assertFalse(result);
    }

    @Test
    void sendAlerte_shouldPersistWhenMesureOutOfRange() {
        Mesure mesure = buildMesure(new BigDecimal("29.00"), new BigDecimal("70.00"));
        Capteur capteur = buildCapteur();
        when(jpaCapteurRepository.findByIdWithEntrepotAndPays(mesure.getCapteur().getIdCapteur()))
            .thenReturn(Optional.of(capteur));
        when(jpaLotRepository.findAllByEntrepotIdEntrepotAndEmplacementIgnoreCase(any(), any()))
            .thenReturn(List.of(buildLot()));

        alertePort.sendAlerte(mesure);

        verify(jpaAlerteRepository).save(any(Alerte.class));
        verify(jpaGenereAlerteRepository).save(any(GenereAlerte.class));
    }

    @Test
    void sendAlerte_shouldNotPersistWhenMesureInRange() {
        Mesure mesure = buildMesure(new BigDecimal("29.00"), new BigDecimal("55.00"));
        when(jpaCapteurRepository.findByIdWithEntrepotAndPays(mesure.getCapteur().getIdCapteur()))
            .thenReturn(Optional.of(buildCapteur()));

        alertePort.sendAlerte(mesure);

        verify(jpaAlerteRepository, never()).save(any(Alerte.class));
        verify(jpaGenereAlerteRepository, never()).save(any(GenereAlerte.class));
    }

    private Mesure buildMesure(BigDecimal temperature, BigDecimal humidite) {
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(UUID.randomUUID());

        Mesure mesure = new Mesure();
        mesure.setCapteur(capteur);
        mesure.setTemperature(temperature);
        mesure.setHumidite(humidite);
        return mesure;
    }

    private Capteur buildCapteur() {
        Entrepot entrepot = new Entrepot();
        entrepot.setIdEntrepot(UUID.randomUUID());

        Capteur capteur = new Capteur();
        capteur.setEmplacementPrecis("A");
        capteur.setEntrepot(entrepot);
        return capteur;
    }

    private Lot buildLot() {
        Lot lot = new Lot();
        lot.setIdLot(UUID.randomUUID());
        lot.setEmplacement("A");
        return lot;
    }
}

