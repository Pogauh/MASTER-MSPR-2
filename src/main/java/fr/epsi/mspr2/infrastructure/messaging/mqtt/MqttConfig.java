package fr.epsi.mspr2.infrastructure.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * Raccordement au broker MQTT. Les canaux sont toujours déclarés ; la connexion au broker
 * (clients entrant et sortant) peut être coupée avec {@code mqtt.enabled=false}.
 */
@Configuration
@IntegrationComponentScan
public class MqttConfig {

    private static final String BROKER_ENABLED = "mqtt.enabled";

    @Value("${mqtt.topic}")
    private String mqttTopic;

    @Value("${mqtt.broker-uri}")
    private String mqttBrokerUri;

    @Value("${mqtt.outbound-default-topic}")
    private String mqttOutboundDefaultTopic;

    // 1. La connexion physique au Broker Mosquitto
    @Bean
    @ConditionalOnProperty(name = BROKER_ENABLED, havingValue = "true", matchIfMissing = true)
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttBrokerUri});
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    // 2. Le "tuyau" (le Channel) par lequel arrivent les mesures
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 3. L'adaptateur qui transforme les paquets MQTT en messages Java
    @Bean
    @ConditionalOnProperty(name = BROKER_ENABLED, havingValue = "true", matchIfMissing = true)
    public MessageProducer inbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("java-backend-client", mqttClientFactory, mqttTopic);

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 4. Le tuyau de sortie
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    // 5. L'émetteur physique vers Mosquitto
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    @ConditionalOnProperty(name = BROKER_ENABLED, havingValue = "true", matchIfMissing = true)
    public MessageHandler mqttOutbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("java-backend-sender", mqttClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(mqttOutboundDefaultTopic);
        return messageHandler;
    }
}
