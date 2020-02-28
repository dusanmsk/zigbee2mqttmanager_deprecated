package org.msk.loxone.zigbee2mqtt.manager.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;

@SpringComponent
@UIScope
@RequiredArgsConstructor
public class MappingForm extends VerticalLayout {

    private final DeviceConfiguration deviceConfiguration;

    private Grid<MappingGridModel> mappingGrid = new Grid(MappingGridModel.class);
    private Set<MappingGridModel> mappingGridModel = new HashSet<>();
    private ValueMappingSourceForm.ItemDoubleclickListener doubleclickListener;

    @PostConstruct
    void init() {
        add(mappingGrid);
        mappingGridModel = deviceConfiguration.getMappings().stream().map(this::convert).collect(Collectors.toSet());
        mappingGrid.addComponentColumn(item -> new Button("Actions"));
        Shortcuts.addShortcutListener(mappingGrid, this::deleteSelectedMapping, Key.DELETE);

    }

    private void updateMappingGrid() {
        mappingGrid.setItems(mappingGridModel);
    }

    private void deleteSelectedMapping() {
        mappingGridModel.removeAll(mappingGrid.getSelectedItems());
        updateMappingGrid();
    }

    private MappingGridModel convert(DeviceConfiguration.MappingDefinition mappingDefinition) {
        return MappingGridModel.builder()
                .manufacturer(mappingDefinition.getDeviceType().getManufacturerName())
                .modelId(mappingDefinition.getDeviceType().getModelID())
                .path(mappingDefinition.getPath())
                .originalValue(mappingDefinition.getOriginalValue())
                .translatedValue(mappingDefinition.getTranslatedValue())
                .build();
    }

    public void setDoubleclickListener(ValueMappingSourceForm.ItemDoubleclickListener doubleclickListener) {
        this.doubleclickListener = doubleclickListener;
    }

    public void addMapping(MappingGridModel item) {
        mappingGridModel.remove(item);
        mappingGridModel.add(item);
    }

    public interface ItemDoubleclickListener {

        void process(MappingGridModel model);
    }

    public void save() {
        deviceConfiguration.setMappings(mappingGridModel.stream().map(this::toConfiguration).collect(Collectors.toList()));
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
        @EqualsAndHashCode.Exclude
        private String translatedValue;
    }

}
