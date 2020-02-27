package org.msk.loxone.zigbee2mqtt.zigbee.loxone;

import static java.lang.String.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoxoneGateway {

    private final ZigbeeService zigbeeService;
    private final ObjectMapper objectMapper;
    private final DeviceConfiguration deviceConfiguration;

    @PostConstruct
    private void init() {
        zigbeeService.addZigbeeDeviceMessageListener(this::processMessage);

        // extract payloads
        // translate payloads
        // send to loxone
    }

    private void processMessage(String topic, byte[] message) {
        try {
            String deviceName = topic.split("/")[1];
            HashMap json = objectMapper.readValue(message, HashMap.class);
            processMap(deviceName, "", json);
        } catch (Exception e) {
            log.error("Failed to deserialize message", e);
        }
    }

    private void processMap(String deviceName, String path, Map json) {
        json.keySet().forEach(key -> {
            Object value = json.get(key);
            if (value instanceof Map) {
                processMap(deviceName, format("%s/%s", path, key.toString()), (Map) value);
            } else if (value instanceof Collection) {
                ((Collection) value).forEach(i -> {
                    sendToLoxone(deviceName, format("%s/%s", path, key.toString()), i.toString());
                });
            } else {
                sendToLoxone(deviceName, format("%s/%s", path, key.toString()), value.toString());
            }
        });

    }

    private void sendToLoxone(String deviceName, String path, String value) {
        path = normalize(path);
        value = translateToLoxone(deviceName, path, value);
        log.debug("Sending zigbee device '{}' path '{}' value '{}' to loxone", deviceName, path, value);
    }

    private String translateToLoxone(String deviceName, String path, String value) {
        DeviceConfiguration.DeviceType deviceType = zigbeeService.getDeviceType(deviceName);
        return deviceConfiguration.translateValueForward(deviceType, path, value);
    }

    private String normalize(String key) {
        while (key.contains("//")) {
            key = key.replaceAll("//", "/");
        }
        return key;
    }

}
