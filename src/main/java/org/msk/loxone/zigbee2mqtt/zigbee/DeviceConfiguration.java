package org.msk.loxone.zigbee2mqtt.zigbee;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class DeviceConfiguration {

    public Configuration configuration = new Configuration();

    public void save() throws IOException {
        new ObjectMapper().writeValue(Paths.get("settings.json").toFile(), configuration);
    }

    public void addMapping(DeviceType deviceType, String path, String originalValue, String translatedValue) {
        configuration.valueMappingDefinitions.add(MappingDefinition.builder()
                .deviceType(deviceType)
                .path(path)
                .originalValue(originalValue)
                .translatedValue(translatedValue)
                .build());
    }

    public void setMappings(Collection<MappingDefinition> mappings) {
        configuration.valueMappingDefinitions.clear();
        configuration.valueMappingDefinitions.addAll(mappings);
    }

    public String translateValueForward(DeviceType deviceType, String path, String value) {
        Optional<String> returnValue = configuration.valueMappingDefinitions.stream().filter(i ->
                i.deviceType.equals(deviceType) &&
                i.path.equals(path) &&
                i.originalValue.equals(value))
                .map(i -> i.translatedValue).findFirst();
        return returnValue.orElse(value);
    }

    public String translateValueBackward(DeviceType deviceType, String path, String value) {
        Optional<String> returnValue = configuration.valueMappingDefinitions.stream().filter(i ->
                i.deviceType.equals(deviceType) &&
                        i.path.equals(path) &&
                        i.translatedValue.equals(value))
                .map(i -> i.originalValue).findFirst();
        return returnValue.orElse(value);
    }

    static class Configuration {

        public List<MappingDefinition> valueMappingDefinitions = new ArrayList<>();
    }

    @Builder
    @EqualsAndHashCode
    public static class MappingDefinition {

        public DeviceType deviceType;
        public String path;
        public String originalValue;
        public String translatedValue;
    }

    @Data
    @EqualsAndHashCode
    @Builder
    public static class DeviceType {

        private String modelID;

        private String manufacturerName;

    }

}
