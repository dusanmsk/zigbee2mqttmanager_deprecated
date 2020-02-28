package org.msk.loxone.zigbee2mqtt.manager.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeService;
import org.msk.loxone.zigbee2mqtt.zigbee.loxone.LoxoneGateway;

@SpringComponent
@UIScope
@RequiredArgsConstructor
public class ValueMappingSourceForm extends VerticalLayout {

    private static final int MAX_ENTRIES = 1000;

    private final LoxoneGateway loxoneGateway;
    private final ZigbeeService zigbeeService;
    Set<ListeningGridModel> listeningGridModel = Collections.newSetFromMap(new LinkedHashMap<ListeningGridModel, Boolean>() {

        protected boolean removeEldestEntry(Map.Entry<ListeningGridModel, Boolean> eldest) {
            return size() > MAX_ENTRIES;
        }
    });
    private ItemDoubleclickListener doubleclickListener;
    private Grid<ListeningGridModel> listeningGrid = new Grid(ListeningGridModel.class);
    private String filterText = "";

    @PostConstruct
    public void init() {
        listeningGrid.addItemDoubleClickListener(this::onListeningGridDoubleClick);
        loxoneGateway.addListener(this::rememberLoxoneValue);
        TextField filterTextField = new TextField();
        filterTextField.setValueChangeMode(ValueChangeMode.EAGER);
        filterTextField.addValueChangeListener(this::setFilter);
        add(new HorizontalLayout(new Label("Filter:"), filterTextField, new Button("Refresh", this::refeshListeningGrid), new Button("Clear", this::clearListeningGrid)));
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

    private void onListeningGridDoubleClick(ItemDoubleClickEvent<ListeningGridModel> listeningGridModelItemDoubleClickEvent) {
        doubleclickListener.process(listeningGridModelItemDoubleClickEvent.getItem());
    }

    private void refeshListeningGrid(ClickEvent<Button> buttonClickEvent) {
        listeningGrid.setItems(filter(listeningGridModel, filterText));
    }

    private Collection<ListeningGridModel> filter(Set<ListeningGridModel> model, String filterText) {
        if(filterText.isBlank()) {
            return model;
        }
        return model.stream().filter(m->
                m.getManufacturer().contains(filterText) ||
                        m.getModelId().contains(filterText) ||
                        m.getPath().contains(filterText) ||
                        m.getValue().contains(filterText)).collect(Collectors.toList());
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

    public void setDoubleclickListener(ItemDoubleclickListener doubleclickListener) {
        this.doubleclickListener = doubleclickListener;
    }

    public Optional<ListeningGridModel> getSelected() {
        return listeningGrid.getSelectedItems().stream().findFirst();
    }

    public interface ItemDoubleclickListener {

        void process(ListeningGridModel model);
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
