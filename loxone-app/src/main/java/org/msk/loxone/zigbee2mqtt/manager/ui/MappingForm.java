package org.msk.loxone.zigbee2mqtt.manager.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.msk.zigbee2mqtt.configuration.DeviceConfiguration;

import static java.lang.String.format;

import org.msk.zigbee2mqtt.configuration.MappingDefinition;

@SpringComponent
@UIScope
@RequiredArgsConstructor
public class MappingForm extends VerticalLayout {

    private final DeviceConfiguration deviceConfiguration;

    private Grid<MappingDefinition> mappingGrid;
    private Set<MappingDefinition> mappingGridModel = new HashSet<>();

    @PostConstruct
    void init() {
        mappingGrid=new Grid<>();
        mappingGridModel = deviceConfiguration.getMappings().stream().collect(Collectors.toSet());
        mappingGrid.addColumn(MappingDefinition::getDeviceType).setHeader("Device type").setSortable(true);
        mappingGrid.addColumn(MappingDefinition::getPath).setHeader("Value path").setSortable(true);
        mappingGrid.addColumn(MappingDefinition::getOriginalValue).setHeader("Original value").setSortable(true);
        mappingGrid.addColumn(MappingDefinition::getTranslatedValue).setHeader("Translated value").setSortable(true);
        mappingGrid.addComponentColumn(item -> createDeleteButton(mappingGrid, item));

        Shortcuts.addShortcutListener(mappingGrid, this::deleteSelectedMapping, Key.DELETE);
        updateMappingGrid();

        add(mappingGrid);
    }

    private Button createDeleteButton(Grid<MappingDefinition> grid, MappingDefinition item) {
        return new Button("Delete", clickEvent->{
            mappingGridModel.remove(item);
            updateMappingGrid();
        });
    }

    private void updateMappingGrid() {
        mappingGrid.setItems(mappingGridModel);
    }

    private void deleteSelectedMapping() {
        mappingGridModel.removeAll(mappingGrid.getSelectedItems());
        updateMappingGrid();
    }


    public void addMapping(MappingDefinition definition) {
        mappingGridModel.remove(definition);
        mappingGridModel.add(definition);
        updateMappingGrid();
    }

    public void save() {
        deviceConfiguration.setMappings(mappingGridModel);
    }

}
