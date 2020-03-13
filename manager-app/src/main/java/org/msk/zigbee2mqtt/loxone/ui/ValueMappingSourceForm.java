package org.msk.zigbee2mqtt.loxone.ui;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.RequiredArgsConstructor;
import org.msk.zigbee2mqtt.ZigbeeService;
import org.msk.zigbee2mqtt.configuration.DeviceType;
import org.msk.zigbee2mqtt.configuration.MappingDefinition;
import org.msk.zigbee2mqtt.loxone.LoxoneGateway;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
@RequiredArgsConstructor
public class ValueMappingSourceForm extends VerticalLayout {

    private static final int MAX_ENTRIES = 1000;

    private final LoxoneGateway loxoneGateway;
    private final ZigbeeService zigbeeService;
    Set<MappingDefinition> listeningGridModel = Collections.newSetFromMap(new LinkedHashMap<MappingDefinition, Boolean>() {

        protected boolean removeEldestEntry(Map.Entry<MappingDefinition, Boolean> eldest) {
            return size() > MAX_ENTRIES;
        }
    });
    private Grid<MappingDefinition> listeningGrid;
    private String filterText = "";

    @PostConstruct
    public void init() {
        loxoneGateway.addListener(this::rememberLoxoneValue);
        TextField filterTextField = new TextField();
        filterTextField.setValueChangeMode(ValueChangeMode.EAGER);
        filterTextField.addValueChangeListener(this::setFilter);
        add(new HorizontalLayout(new Label("Filter:"), filterTextField, new Button("Refresh", this::refeshListeningGrid),
                new Button("Clear", this::clearListeningGrid)));
        listeningGrid = new Grid();
        listeningGrid.addColumn(MappingDefinition::getDeviceType).setHeader("Device type").setSortable(true);
        listeningGrid.addColumn(MappingDefinition::getPath).setHeader("Value path").setSortable(true);
        listeningGrid.addColumn(MappingDefinition::getOriginalValue).setHeader("Original value").setSortable(true);
        add(listeningGrid);
    }

    private void setFilter(AbstractField.ComponentValueChangeEvent<TextField, String> textFieldStringComponentValueChangeEvent) {
        filterText = textFieldStringComponentValueChangeEvent.getValue();
        refeshListeningGrid(null);
    }

    private void clearListeningGrid(ClickEvent<Button> buttonClickEvent) {
        listeningGridModel.clear();
        refeshListeningGrid(buttonClickEvent);
    }

    private void refeshListeningGrid(ClickEvent<Button> buttonClickEvent) {
        listeningGrid.setItems(filter(listeningGridModel, filterText));
    }

    private Collection<MappingDefinition> filter(Set<MappingDefinition> model, String filterText) {
        if (filterText.isBlank()) {
            return model;
        }
        return model.stream().filter(mappingDefinition -> mappingDefinition.getDeviceType().getManufacturerName().contains(filterText) ||
                mappingDefinition.getDeviceType().getModelID().contains(filterText) ||
                mappingDefinition.getPath().contains(filterText) ||
                mappingDefinition.getOriginalValue().contains(filterText)).collect(Collectors.toList());
    }

    private void rememberLoxoneValue(String deviceName, String path, String value) {
        DeviceType deviceType = zigbeeService.getDeviceType(deviceName);
        if (deviceType != null) {
            listeningGridModel.add(new MappingDefinition(
                    deviceType,
                    path,
                    value,
                    null));
        }
    }

    public Set<MappingDefinition> getSelected() {
        return listeningGrid.getSelectedItems();
    }

}
