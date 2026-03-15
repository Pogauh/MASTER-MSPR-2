package fr.epsi.mspr2.infrastructure.config;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {

    /**
     * Envoie un message sur un topic spécifique
     * @param data Le contenu du message (ex: JSON)
     * @param topic Le topic MQTT (ex: "futurekawa/alerte")
     */
    void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);
}