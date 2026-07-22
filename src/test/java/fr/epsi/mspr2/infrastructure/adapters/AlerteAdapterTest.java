package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.infrastructure.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlerteAdapterTest {

    private JpaAlerteRepository jpaAlerteRepository;
    private JpaCapteurRepository jpaCapteurRepository;
    private JpaHistoriqueRepository jpaHistoriqueRepository;
    private JpaLotRepository jpaLotRepository;
    private JpaMesureRepository jpaMesureRepository;
    private JavaMailSender mailSender;
    private AlerteAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaAlerteRepository = mock(JpaAlerteRepository.class);
        jpaCapteurRepository = mock(JpaCapteurRepository.class);
        jpaHistoriqueRepository = mock(JpaHistoriqueRepository.class);
        jpaLotRepository = mock(JpaLotRepository.class);
        jpaMesureRepository = mock(JpaMesureRepository.class);
        mailSender = mock(JavaMailSender.class);

        adapter = new AlerteAdapter(
                jpaAlerteRepository, jpaCapteurRepository, jpaHistoriqueRepository,
                jpaLotRepository, jpaMesureRepository, mailSender
        );

        // Inject @Value fields via ReflectionTestUtils
        ReflectionTestUtils.setField(adapter, "emailResponsable", "test@example.com");
        ReflectionTestUtils.setField(adapter, "tempIdeale", new BigDecimal("18.0"));
        ReflectionTestUtils.setField(adapter, "humiIdeale", new BigDecimal("60.0"));
        ReflectionTestUtils.setField(adapter, "toleranceTemp", new BigDecimal("5.0"));
        ReflectionTestUtils.setField(adapter, "toleranceHumi", new BigDecimal("10.0"));
    }

    private Mesure buildMesure(BigDecimal temp, BigDecimal humi) {
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(UUID.randomUUID());
        Mesure m = new Mesure();
        m.setTemperature(temp);
        m.setHumidite(humi);
        m.setCapteur(capteur);
        return m;
    }

    @Test
    void isCompatible_shouldReturnTrueWhenTemperatureTooHigh() {
        Mesure m = buildMesure(new BigDecimal("30.0"), new BigDecimal("60.0"));
        assertThat(adapter.isCompatible(m)).isTrue();
    }

    @Test
    void isCompatible_shouldReturnTrueWhenTemperatureTooLow() {
        Mesure m = buildMesure(new BigDecimal("5.0"), new BigDecimal("60.0"));
        assertThat(adapter.isCompatible(m)).isTrue();
    }

    @Test
    void isCompatible_shouldReturnTrueWhenHumiditeTooHigh() {
        Mesure m = buildMesure(new BigDecimal("18.0"), new BigDecimal("80.0"));
        assertThat(adapter.isCompatible(m)).isTrue();
    }

    @Test
    void isCompatible_shouldReturnTrueWhenHumiditeTooLow() {
        Mesure m = buildMesure(new BigDecimal("18.0"), new BigDecimal("40.0"));
        assertThat(adapter.isCompatible(m)).isTrue();
    }

    @Test
    void isCompatible_shouldReturnFalseWhenInRange() {
        // temp: 18 ± 5 → [13, 23], humi: 60 ± 10 → [50, 70]
        Mesure m = buildMesure(new BigDecimal("18.0"), new BigDecimal("60.0"));
        assertThat(adapter.isCompatible(m)).isFalse();
    }

    @Test
    void isCompatible_shouldThrowWhenTemperatureIsNull() {
        Mesure m = buildMesure(null, new BigDecimal("60.0"));
        assertThatThrownBy(() -> adapter.isCompatible(m))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sendAlerte_shouldDoNothingWhenLessThan5Mesures() {
        Mesure m = buildMesure(new BigDecimal("30.0"), new BigDecimal("60.0"));
        when(jpaMesureRepository.findTop5ByCapteurIdCapteurOrderByDateMesureDesc(m.getCapteur().getIdCapteur()))
                .thenReturn(List.of(m, m, m)); // only 3

        adapter.sendAlerte(m);

        verifyNoInteractions(jpaAlerteRepository);
    }

    @Test
    void sendAlerte_shouldDoNothingWhenNotAllInAnomalie() {
        Mesure mOk = buildMesure(new BigDecimal("18.0"), new BigDecimal("60.0")); // in range
        Mesure mBad = buildMesure(new BigDecimal("30.0"), new BigDecimal("60.0")); // out of range
        mBad.setCapteur(mOk.getCapteur()); // same capteur

        when(jpaMesureRepository.findTop5ByCapteurIdCapteurOrderByDateMesureDesc(mBad.getCapteur().getIdCapteur()))
                .thenReturn(List.of(mBad, mOk, mBad, mBad, mBad)); // not all in anomalie

        adapter.sendAlerte(mBad);

        verifyNoInteractions(jpaAlerteRepository);
    }

    @Test
    void sendAlerte_shouldCreateAlerteAndSendMailWhenAllInAnomalie() {
        UUID capteurId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(capteurId);
        Zone zone = new Zone();
        zone.setIdZone(zoneId);
        capteur.setZone(zone);

        // Temp très haute → hors plage
        Mesure mBad = new Mesure();
        mBad.setTemperature(new BigDecimal("30.0"));
        mBad.setHumidite(new BigDecimal("60.0"));
        mBad.setCapteur(capteur);

        List<Mesure> cinqMesures = List.of(mBad, mBad, mBad, mBad, mBad);

        when(jpaMesureRepository.findTop5ByCapteurIdCapteurOrderByDateMesureDesc(capteurId)).thenReturn(cinqMesures);
        when(jpaCapteurRepository.findByCapteurId(capteurId)).thenReturn(Optional.of(capteur));

        Alerte savedAlerte = new Alerte();
        savedAlerte.setIdAlerte(UUID.randomUUID());
        savedAlerte.setTypeAlerte(TypeAlerteEnum.HAUTETEMPERATURE);
        when(jpaAlerteRepository.save(any(Alerte.class))).thenReturn(savedAlerte);
        when(jpaLotRepository.findByZoneIdZone(zoneId)).thenReturn(List.of());

        adapter.sendAlerte(mBad);

        verify(jpaAlerteRepository).save(any(Alerte.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}

