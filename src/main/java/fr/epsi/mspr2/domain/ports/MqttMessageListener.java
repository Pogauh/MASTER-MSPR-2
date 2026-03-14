package fr.epsi.mspr2.domain.ports;

public interface MqttMessageListener {
    void onMessage(String topic, String message);
}
