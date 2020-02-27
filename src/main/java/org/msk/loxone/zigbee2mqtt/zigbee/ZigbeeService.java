package org.msk.loxone.zigbee2mqtt.zigbee;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
public class ZigbeeService {

    private final MqttService mqttService;
    private final String ZIGBEE2MQTT_PREFIX = "zigbee2mqtt"; // todo dusan.zatkovsky configuration
    private Set<MqttMessageListener> zigbeeDeviceMessageListeners = new HashSet<>();
    private List<ZigbeeDevice> devices;

    @PostConstruct
    void init() throws MqttException {
        mqttService.subscribe(ZIGBEE2MQTT_PREFIX+"/#", this::zigbeeMessageReceived);
    }

    private void zigbeeMessageReceived(String topic, MqttMessage mqttMessage) throws IOException {
        topic = topic.replace(ZIGBEE2MQTT_PREFIX+"/", "");
        // device list message
        if (topic.equals("bridge/config/devices")) {
            processDevicesListMessage(mqttMessage.getPayload());
        }
        // zigbee device generic message
        if (StringUtils.countOccurrencesOf(topic, "/") == 0) {
            processZigbeeDeviceMessage(topic, mqttMessage.getPayload());
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void refreshDeviceList() {
        mqttService.publish(ZIGBEE2MQTT_PREFIX + "/bridge/config/devices/get", "");
    }

    private void processZigbeeDeviceMessage(String deviceName, byte[] payload) {
        log.debug("Dispatching zigbee device message to {} listeners", zigbeeDeviceMessageListeners.size());
        zigbeeDeviceMessageListeners.forEach(l -> l.processMessage(deviceName, payload));
    }

    private void processDevicesListMessage(byte[] payload) throws IOException {
        ZigbeeDevice[] deviceArray = new ObjectMapper().readValue(payload, ZigbeeDevice[].class);
        devices = Arrays.asList(deviceArray);
        log.debug("Received device list info, known devices: {}", devices.size());
    }

    public void addZigbeeDeviceMessageListener(MqttMessageListener listener) {
        zigbeeDeviceMessageListeners.add(listener);
    }

    public List<ZigbeeDevice> getDeviceList() {
        return devices;
    }

    public void renameDevice(String oldFriendlyName, String newFriendlyName) {
        mqttService.publish("zigbee2mqtt/bridge/config/rename", formatRenameDevicePayload(oldFriendlyName, newFriendlyName));
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
        void processMessage(String deviceName, byte[] message);
    }
}
