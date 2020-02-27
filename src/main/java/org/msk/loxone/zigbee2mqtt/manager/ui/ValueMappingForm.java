package org.msk.loxone.zigbee2mqtt.manager.ui;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;

public class ValueMappingForm extends VerticalLayout {
/*
    private final List<DeviceConfiguration.ValueMapping> gridModel;
    private Grid<DeviceConfiguration.ValueMapping> grid;
    private TextField originalValueEditor;
    private TextField translatedValueEditor;
    private TextField valueNameEditor;

    public ValueMappingForm(List<DeviceConfiguration.ValueMapping> mapping) {
        setupUI();
        gridModel = mapping;
        refreshGrid();
    }

    private void setupUI() {
        grid = new Grid<>(DeviceConfiguration.ValueMapping.class);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        Button addButton = new Button("Add", this::onAddRecord);
        Button removeButton = new Button("Remove", this::onRemoveRecord);

        valueNameEditor = new TextField();
        originalValueEditor = new TextField();
        translatedValueEditor = new TextField();

        add(grid);
        add(new HorizontalLayout(valueNameEditor, originalValueEditor, translatedValueEditor, addButton, removeButton));
    }

    private void onRemoveRecord(ClickEvent<Button> buttonClickEvent) {
        Optional<DeviceConfiguration.ValueMapping> selectedOptional = grid.getSelectedItems().stream().findFirst();
        selectedOptional.ifPresent(selectedMapping -> {
            gridModel.remove(selectedMapping);
            refreshGrid();
        });
    }

    private void refreshGrid() {
        grid.setItems(gridModel);
    }

    private void onAddRecord(ClickEvent<Button> buttonClickEvent) {
        gridModel.add(DeviceConfiguration.ValueMapping.builder()
                .path(valueNameEditor.getValue())
                .originalValue(originalValueEditor.getValue())
                .translatedValue(translatedValueEditor.getValue())
                .build());
        originalValueEditor.setValue("");
        translatedValueEditor.setValue("");
        refreshGrid();
    }

    public List<DeviceConfiguration.ValueMapping> getMappings() {
        return gridModel;
    }

 */
}
