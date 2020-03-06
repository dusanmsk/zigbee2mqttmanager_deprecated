package org.msk.zigbee2mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.msk.zigbee2mqtt.configuration.DeviceType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@Component
@Slf4j
@RequiredArgsConstructor
public class ZigbeeService {

    private final MqttService mqttService;
    private final String ZIGBEE2MQTT_PREFIX = "zigbee2mqtt"; // todo dusan.zatkovsky configuration
    private Set<MqttMessageListener> zigbeeDeviceMessageListeners = new HashSet<>();
    private List<ZigbeeDevice> devices = new ArrayList<>();
    private int remainingSecondsToDisable;
    private BridgeConfig bridgeConfig;

    @PostConstruct
    void init() throws MqttException {
        mqttService.subscribe(zigbeeTopic("/+"), this::processZigbeeDeviceMessage);
        mqttService.subscribe(zigbeeTopic("/bridge/config/devices"), this::processDevicesListMessage);
        mqttService.subscribe(zigbeeTopic("/bridge/config"), this::processConfigMessage);
        mqttService.subscribe(zigbeeTopic("/#"), this::logZigbeeMessage);
    }

    @Scheduled(fixedDelay = 30000)
    public void refreshDeviceList() {
        mqttService.publish(ZIGBEE2MQTT_PREFIX + "/bridge/config/devices/get", "");
    }

    private void processZigbeeDeviceMessage(String topic, MqttMessage mqttMessage) {
        String deviceName = topic.split("/")[1];
        log.debug("Dispatching zigbee device message to {} listeners", zigbeeDeviceMessageListeners.size());
        zigbeeDeviceMessageListeners.forEach(l -> l.processMessage(deviceName, mqttMessage.getPayload()));
    }

    private void processDevicesListMessage(String topic, MqttMessage mqttMessage) throws IOException {
        ZigbeeDevice[] deviceArray = new ObjectMapper().readValue(mqttMessage.getPayload(), ZigbeeDevice[].class);
        devices = Arrays.asList(deviceArray);
        log.debug("Received device list info, known devices: {}", devices.size());
    }

    private void processConfigMessage(String topic, MqttMessage mqttMessage) throws IOException {
        log.debug("Received bridge config");
        bridgeConfig = new ObjectMapper().readValue(mqttMessage.getPayload(), BridgeConfig.class);
    }

    public void addZigbeeDeviceMessageListener(MqttMessageListener listener) {
        zigbeeDeviceMessageListeners.add(listener);
    }

    @Scheduled(fixedDelay = 5000)
    void autoDisableJoin() {
        if(remainingSecondsToDisable > 0 ) {
            remainingSecondsToDisable -= 5;       // note must match fixedDelay
        }
        if (remainingSecondsToDisable < 0) {
            mqttService.publish("zigbee2mqtt/bridge/config/permit_join", "false");
            remainingSecondsToDisable = 0;
        }
    }

    private void logZigbeeMessage(String topic, MqttMessage mqttMessage) {
        log.debug("Received zigbee message {} : {}", topic, new String(mqttMessage.getPayload()));
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
     * @param deviceName
     * @return Device type or null if not found
     */
    public DeviceType getDeviceType(String deviceName) {
        return devices.stream().filter(d -> d.getFriendlyName().equals(deviceName)).findFirst()
                .map(d -> DeviceType.builder()
                        .manufacturerName(d.getManufacturerName())
                        .modelID(d.getModelID())
                        .build())
                .orElse(null);

    }

    public void enableJoin(boolean enabled, int autoDisableSeconds) {
        mqttService.publish("zigbee2mqtt/bridge/config/permit_join", enabled ? "true" : "false");
        remainingSecondsToDisable = autoDisableSeconds;
    }

    private String zigbeeTopic(String topic) {
        return (ZIGBEE2MQTT_PREFIX + topic).replaceAll("//", "/");
    }

    public boolean isJoinEnabled() {
        if(bridgeConfig != null) {
            return bridgeConfig.permitJoin;
        }
        return false;
    }

    public int getJoinTimeout() {
        return remainingSecondsToDisable;
    }

    public interface MqttMessageListener {

        void processMessage(String deviceName, byte[] message);
    }

    @Getter
    @Setter
    @NoArgsConstructor  // jackson
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BridgeConfig {
        @JsonProperty("permit_join")
        boolean permitJoin;
    }
}
