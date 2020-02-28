package org.msk.loxone.zigbee2mqtt.manager.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeDevice;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeService;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Route
@SpringComponent
@RequiredArgsConstructor
@UIScope
@Slf4j
public class MainView extends VerticalLayout {

    private final ZigbeeService zigbeeService;
    private final DeviceConfiguration deviceConfiguration;

    private Grid<ZigbeeDevice> zigbeeDeviceGrid;

    @PostConstruct
    public void init() {
        setupUI();
    }

    private void setupUI() {

        Button refreshButton = new Button("Refresh");
        refreshButton.addClickListener(e -> refreshDeviceList());

        Button editButton = new Button("Edit");
        editButton.addClickListener(e -> editButtonClicked());
        editButton.setEnabled(false);

        Button saveButton = new Button("Save");
        saveButton.addClickListener(e -> saveButtonClicked());

        zigbeeDeviceGrid = new Grid<>(ZigbeeDevice.class);
        zigbeeDeviceGrid.addSelectionListener(e -> {
            editButton.setEnabled(zigbeeDeviceGrid.getSelectedItems().size() == 1);
        });

        GridContextMenu<ZigbeeDevice> deviceContextMenu = zigbeeDeviceGrid.addContextMenu();
        deviceContextMenu.addItem("Show detail", e -> showDeviceDetail(e));
        deviceContextMenu.addItem("Create rule", e -> createRule(e));

        add(new Text("Zigbee device list:"));
        add(refreshButton);
        add(zigbeeDeviceGrid);

        add(editButton);

        add(new Button("Edit mappings", e->onEditMappings()));

        add(saveButton);

    }

    private void onEditMappings() {
        UI.getCurrent().navigate(EditMappingForm2.class);
    }

    private void refreshDeviceList() {
        zigbeeDeviceGrid.setItems(zigbeeService.getDeviceList());
    }

    private void saveButtonClicked() {
        try {
            deviceConfiguration.save();
        } catch (Exception e) {
            log.error("Failed to save configuration", e);
            Notification.show("Failed to save configuration", 5000, Notification.Position.MIDDLE);
        }
    }

    private void editButtonClicked() {
        getUI().ifPresent(ui->ui.navigate(EditDeviceForm.class));
        /*
        Optional<ZigbeeDevice> selectedDevice = zigbeeDeviceGrid.getSelectedItems().stream().findFirst();
        if (selectedDevice.isPresent()) {
            // todo dusan.zatkovsky edit
        }
         */

    }

    private void createRule(GridContextMenu.GridContextMenuItemClickEvent<ZigbeeDevice> event) {
        // todo dusan.zatkovsky 
    }

    private void showDeviceDetail(GridContextMenu.GridContextMenuItemClickEvent<ZigbeeDevice> event) {
        // todo dusan.zatkovsky
        Notification.show("TODO show detail " + event.getItem().get());
    }

    public Optional<ZigbeeDevice> getSelectedDevice() {
        return zigbeeDeviceGrid.getSelectedItems().stream().findFirst();
    }


}
