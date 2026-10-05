package fr.epsi.mspr2.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.models.NouvelleMesure;
import fr.epsi.mspr2.domain.services.MesureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MqttMesureListenerTest {

    // même configuration Jackson que celle de Spring Boot (modules Java 8, propriétés inconnues ignorées)
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Mock
    MesureService mesureService;

    MqttMesureListener listener;
    final UUID idCapteur = UUID.fromString("b793e242-053a-4def-9cd8-96c990a586c9");

    @BeforeEach
    void setUp() {
        listener = new MqttMesureListener(mesureService, objectMapper);
    }

    private String payload() {
        return """
                {"temperature":27.50,"humidite":56.20,"dateMesure":"2026-03-15T16:00:00","idCapteur":"%s"}
                """.formatted(idCapteur);
    }

    @Test
    void unMessageEnOctets_creeUneMesure() {
        listener.handleMessage(MessageBuilder.withPayload(payload().getBytes(StandardCharsets.UTF_8)).build());

        ArgumentCaptor<NouvelleMesure> mesure = ArgumentCaptor.forClass(NouvelleMesure.class);
        verify(mesureService).createMesure(mesure.capture());
        assertThat(mesure.getValue().temperature()).isEqualByComparingTo("27.50");
        assertThat(mesure.getValue().humidite()).isEqualByComparingTo("56.20");
        assertThat(mesure.getValue().dateMesure()).isEqualTo(LocalDateTime.of(2026, 3, 15, 16, 0));
        assertThat(mesure.getValue().idCapteur()).isEqualTo(idCapteur);
    }

    @Test
    void unMessageTextuel_creeUneMesure() {
        listener.handleMessage(MessageBuilder.withPayload(payload()).build());

        verify(mesureService).createMesure(new NouvelleMesure(
                new BigDecimal("27.50"), new BigDecimal("56.20"), LocalDateTime.of(2026, 3, 15, 16, 0), idCapteur));
    }

    @Test
    void lesChampsInconnusSontIgnores() {
        listener.handleMessage(MessageBuilder.withPayload(
                "{\"temperature\":20,\"humidite\":50,\"idCapteur\":\"%s\",\"batterie\":87}".formatted(idCapteur)).build());

        verify(mesureService).createMesure(new NouvelleMesure(new BigDecimal("20"), new BigDecimal("50"), null, idCapteur));
    }

    @Test
    void lesChampsAbsentsSontTransmisTelsQuelsAuService() {
        listener.handleMessage(MessageBuilder.withPayload("{\"temperature\":20}").build());

        verify(mesureService).createMesure(new NouvelleMesure(new BigDecimal("20"), null, null, null));
    }

    @Test
    void unMessageIllisible_estIgnoreSansErreur() {
        assertThatCode(() -> listener.handleMessage(MessageBuilder.withPayload("pas du json").build()))
                .doesNotThrowAnyException();

        verifyNoInteractions(mesureService);
    }

    @Test
    void unIdentifiantDeCapteurMalForme_estIgnoreSansErreur() {
        assertThatCode(() -> listener.handleMessage(MessageBuilder.withPayload(
                "{\"temperature\":20,\"humidite\":50,\"idCapteur\":\"abc\"}").build())).doesNotThrowAnyException();

        verifyNoInteractions(mesureService);
    }

    @Test
    void uneErreurDuServiceNeRemontePasJusqueAuBroker() {
        doThrow(new IllegalArgumentException("idCapteur est obligatoire")).when(mesureService).createMesure(any());

        assertThatCode(() -> listener.handleMessage(MessageBuilder.withPayload(payload()).build())).doesNotThrowAnyException();

        verify(mesureService).createMesure(any(NouvelleMesure.class));
    }
}
