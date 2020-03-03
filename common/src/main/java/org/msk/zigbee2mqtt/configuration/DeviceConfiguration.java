package org.msk.zigbee2mqtt.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeviceConfiguration {

    public Configuration configuration = new Configuration();
    private File cfgFile = Paths.get("settings.json").toFile();

    public void save() throws IOException {
        new ObjectMapper().writeValue(cfgFile, configuration);
    }

    @PostConstruct
    public void load()  {
        try {
            configuration = new ObjectMapper().readValue(cfgFile, Configuration.class);
            log.info("Loaded configuration from file {}", cfgFile);
        } catch (Exception e) {
            log.error("Failed to load configuration, creating empty one", e);
            configuration = new Configuration();
        }
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
        Optional<String> returnValue = configuration.valueMappingDefinitions.stream().filter(i -> i.deviceType.equals(deviceType) &&
                i.path.equals(path) &&
                i.originalValue.equals(value))
                .map(i -> i.translatedValue).findFirst();
        return returnValue.orElse(value);
    }

    public String translateValueBackward(DeviceType deviceType, String path, String value) {
        Optional<String> returnValue = configuration.valueMappingDefinitions.stream().filter(i -> i.deviceType.equals(deviceType) &&
                i.path.equals(path) &&
                i.translatedValue.equals(value))
                .map(i -> i.originalValue).findFirst();
        return returnValue.orElse(value);
    }

    public Set<MappingDefinition> getMappings() {
        return configuration.valueMappingDefinitions;
    }

    static class Configuration {

        public Set<MappingDefinition> valueMappingDefinitions = new HashSet<>();
    }

}
