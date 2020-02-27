package org.msk.loxone.zigbee2mqtt.zigbee;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class ZigbeeService {

    private final MqttClient mqttClient;
    private final String ZIGBEE2MQTT_PREFIX = "zigbee2mqtt"; // todo dusan.zatkovsky configuration
    private Set<MqttMessageListener> zigbeeDeviceMessageListeners = new HashSet<>();
    private List<ZigbeeDevice> devices;

    public ZigbeeService() throws MqttException {
        mqttClient = new MqttClient("tcp://192.168.17.31", "loxone_zigbee_gw");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);

        if (!mqttClient.isConnected()) {
            mqttClient.connect(options);
        }
        mqttClient.subscribe(ZIGBEE2MQTT_PREFIX + "/#");
        mqttClient.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable throwable) {
                log.warn("Lost MQTT connection", throwable);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                try {
                    dispatchMessage(topic, mqttMessage.getPayload());
                } catch (Exception e) {
                    log.error("Failed to process mqtt message", e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });

    }

    @Scheduled(fixedDelay = 30000)
    public void refreshDeviceList() {
        sendMqtt(ZIGBEE2MQTT_PREFIX + "/bridge/config/devices/get", "");
    }

    private void dispatchMessage(String topic, byte[] payload) throws IOException {
        topic = topic.replace(ZIGBEE2MQTT_PREFIX, "");
        // device list message
        if (topic.equals("/bridge/config/devices")) {
            processDevicesListMessage(payload);
        }
        // zigbee device generic message
        if (StringUtils.countOccurrencesOf(topic, "/") == 1) {
            processZigbeeDeviceMessage(topic, payload);
        }
    }

    private void processZigbeeDeviceMessage(String topic, byte[] payload) {
        log.debug("Dispatching zigbee device message to {} listeners", zigbeeDeviceMessageListeners.size());
        zigbeeDeviceMessageListeners.forEach(l -> l.processMessage(topic, payload));
    }

    private void processDevicesListMessage(byte[] payload) throws IOException {
        ZigbeeDevice[] deviceArray = new ObjectMapper().readValue(payload, ZigbeeDevice[].class);
        devices = Arrays.asList(deviceArray);
        log.debug("Received device list info, known devices: {}", devices.size());
    }

    @Async
    void sendMqtt(String topic, String value) {
        try {
            mqttClient.publish(topic, value.getBytes(Charset.defaultCharset()), 2, false);
        } catch (MqttException e) {
            log.error("Failed to send mqtt message", e);
        }
        log.debug("Sent mqtt {} : {}", topic, value);
    }

    public void addZigbeeDeviceMessageListener(MqttMessageListener listener) {
        zigbeeDeviceMessageListeners.add(listener);
    }

    public List<ZigbeeDevice> getDeviceList() {
        return devices;
    }

    public void renameDevice(String oldFriendlyName, String newFriendlyName) {
        sendMqtt("zigbee2mqtt/bridge/config/rename", formatRenameDevicePayload(oldFriendlyName, newFriendlyName));
    }

    private String formatRenameDevicePayload(String oldFriendlyName, String newFriendlyName) {
        return format("{ \"old\" : \"%s\", \"new\" : \"%s\"  }", oldFriendlyName, newFriendlyName);
    }

    /**
     *
     * @param deviceName
     * @return Device type or null if not found
     */
    public DeviceConfiguration.DeviceType getDeviceType(String deviceName) {
        return devices.stream().filter(d -> d.getFriendlyName().equals(deviceName)).findFirst()
                .map(d -> DeviceConfiguration.DeviceType.builder()
                        .manufacturerName(d.getManufacturerName())
                        .modelID(d.getModelID())
                        .build())
                .orElse(null);

    }

    public interface MqttMessageListener {

        void processMessage(String topic, byte[] message);
    }
}
