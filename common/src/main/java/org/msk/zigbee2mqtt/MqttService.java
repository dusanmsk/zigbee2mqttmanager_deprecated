package org.msk.zigbee2mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.nio.charset.Charset;

import static java.lang.String.format;

@Component
@Slf4j
public class MqttService {

    @Value("${MQTT_HOST}")
    private String MQTT_HOST;

    @Value("${MQTT_PORT}")
    private String MQTT_PORT;

    @Value("${mqtt.client.id}")
    private String MQTT_CLIENT_ID;

    private MqttClient mqttClient;

    boolean initialized = false;

    synchronized public void init() throws MqttException {
        if (initialized) {
            return;
        }
        Assert.notNull(MQTT_HOST, "You must configure MQTT_HOST environment variable");
        Assert.notNull(MQTT_PORT, "You must configure MQTT_PORT environment variable");
        mqttClient = new MqttClient(format("tcp://%s:%s", MQTT_HOST, MQTT_PORT), MQTT_CLIENT_ID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay(5000);
        options.setCleanSession(false);
        mqttClient.connect(options);
        log.info("Connected");
        initialized = true;
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
