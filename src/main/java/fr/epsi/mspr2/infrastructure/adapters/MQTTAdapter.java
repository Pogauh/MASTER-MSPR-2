package fr.epsi.mspr2.infrastructure.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.services.MesureService;
import fr.epsi.mspr2.domain.models.MesureRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class MQTTAdapter {

    private final MesureService serviceMesure;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        Object rawPayload = message.getPayload();
        String payload;

        if (rawPayload instanceof byte[] bytes) {
            payload = new String(bytes, StandardCharsets.UTF_8);
        } else {
            payload = String.valueOf(rawPayload);
        }

        try {
            MesureRequest mesureRequest = objectMapper.readValue(payload, MesureRequest.class);
            serviceMesure.createMesure(mesureRequest);
        } catch (JsonProcessingException e) {
            log.error("Erreur de parsing du message MQTT. Payload: {}", payload, e);
        } catch (Exception e) {
            log.error("Erreur lors du traitement du message MQTT. Payload: {}", payload, e);
        }
    }
}