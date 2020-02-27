package org.msk.loxone.zigbee2mqtt.manager.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.vaadin.flow.component.textfield.TextField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeService;
import org.msk.loxone.zigbee2mqtt.zigbee.loxone.LoxoneGateway;

@Route
@SpringComponent
@RequiredArgsConstructor
public class EditMappingForm extends VerticalLayout {

    private static final String MQTT_TO_LOXONE_TOPIC = "lox_in_TODO"; // todo dusan.zatkovsky config
    private static final int MAX_ENTRIES = 1000;
    private final DeviceConfiguration deviceConfiguration;
    private final LoxoneGateway loxoneGateway;
    private final ZigbeeService zigbeeService;
    Set<ListeningGridModel> listeningGridModel = Collections.newSetFromMap(new LinkedHashMap<ListeningGridModel, Boolean>() {

        protected boolean removeEldestEntry(Map.Entry<ListeningGridModel, Boolean> eldest) {
            return size() > MAX_ENTRIES;
        }
    });
    private Grid<MappingGridModel> mappingGrid = new Grid(MappingGridModel.class);
    private Set<MappingGridModel> mappingGridModel = new HashSet<>();
    private Grid<ListeningGridModel> listeningGrid = new Grid(ListeningGridModel.class);
    private TextField mappedValue = new TextField("Map to:");

    @PostConstruct
    private void setupUI() throws MqttException {
        add(mappingGrid);
        add(new HorizontalLayout(new Button("Refresh", this::refeshListeningGrid),
                mappedValue,
                new Button("Create mapping", this::createNewMapping)));

        add(listeningGrid);
        add(new HorizontalLayout(new Button("Save", this::onSave), new Button("Cancel", this::onCancel)));

        loxoneGateway.addListener(this::rememberLoxoneValue);
    }

    private void refeshListeningGrid(ClickEvent<Button> buttonClickEvent) {
        listeningGrid.setItems(listeningGridModel);
    }

    private void rememberLoxoneValue(String deviceName, String path, String value) {
        DeviceConfiguration.DeviceType deviceType = zigbeeService.getDeviceType(deviceName);
        if (deviceType != null) {
            listeningGridModel.add(new ListeningGridModel(
                    deviceType.getManufacturerName(),
                    deviceType.getModelID(),
                    path,
                    value));
        }
    }

    private void createNewMapping(ClickEvent<Button> buttonClickEvent) {
        Optional<ListeningGridModel> selectedTemplate = listeningGrid.getSelectedItems().stream().findFirst();
        selectedTemplate.ifPresent(listeningGridModel -> {
            mappingGridModel.add(new MappingGridModel(listeningGridModel.getManufacturer(), listeningGridModel.getModelId(), listeningGridModel.getPath(),
                    listeningGridModel.getValue(), mappedValue.getValue()));
            mappingGrid.setItems(mappingGridModel);
            mappedValue.setValue("");
        });
    }

    private void onCancel(ClickEvent<Button> buttonClickEvent) {
        UI.getCurrent().navigate(MainView.class);
    }

    private void onSave(ClickEvent<Button> buttonClickEvent) {
        deviceConfiguration.setMappings(mappingGridModel.stream().map(this::toConfiguration).collect(Collectors.toList()));
        UI.getCurrent().navigate(MainView.class);
    }

    private DeviceConfiguration.MappingDefinition toConfiguration(MappingGridModel m) {
        return DeviceConfiguration.MappingDefinition.builder()
                .deviceType(DeviceConfiguration.DeviceType.builder().manufacturerName(m.getManufacturer()).modelID(m.getModelId()).build())
                .path(m.getPath())
                .originalValue(m.getOriginalValue())
                .translatedValue(m.getTranslatedValue())
                .build();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class MappingGridModel {

        private String manufacturer;
        private String modelId;
        private String path;
        private String originalValue;
        private String translatedValue;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class ListeningGridModel {

        private String manufacturer;
        private String modelId;
        private String path;
        private String value;
    }

}
