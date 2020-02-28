package org.msk.loxone.zigbee2mqtt.zigbee;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
@Slf4j
public class MqttService {

    private final MqttClient mqttClient;

    public MqttService() throws MqttException {
        mqttClient = new MqttClient("tcp://192.168.17.31", "loxone_zigbee_gw2", new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay(5000);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);

        if (!mqttClient.isConnected()) {
            mqttClient.connect(options);
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener) throws MqttException {
        log.debug("Subscribing to {}", topic);
        mqttClient.subscribe(topic, listener);
    }

    public void publish(String topic, String value) {
        try {
            mqttClient.publish(topic, value.getBytes(Charset.defaultCharset()), 2, false);
        } catch (MqttException e) {
            log.error("Failed to send mqtt message", e);
        }
        log.debug("Sent mqtt {} : {}", topic, value);
    }

}
