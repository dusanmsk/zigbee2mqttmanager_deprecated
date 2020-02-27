package org.msk.loxone.zigbee2mqtt.manager.ui;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeDevice;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeService;
import org.springframework.beans.factory.annotation.Autowired;

@Route("edit_device")
@UIScope
public class EditDeviceForm extends VerticalLayout {

    private final ZigbeeDevice device;
    private final ZigbeeService zigbeeService;
    private final DeviceConfiguration deviceConfiguration;
    //private final ValueMappingForm mappingEditor;

    private TextField friendlyName = new TextField("Friendly name:");

    private Button saveButton = new Button("Save");
    private Button cancelButton = new Button("Cancel");


    @Autowired
    public EditDeviceForm(MainView mainView, ZigbeeService zigbeeService, DeviceConfiguration deviceConfiguration) {
        this.device = mainView.getSelectedDevice().get();
        this.zigbeeService = zigbeeService;
        this.deviceConfiguration = deviceConfiguration;
        friendlyName.setValue(device.getFriendlyName());
        saveButton.addClickListener(e -> save());
        cancelButton.addClickListener(e -> cancel());
        //List<DeviceConfiguration.ValueMapping> valueMappings = deviceConfiguration.getMapping(device.getManufacturerName(), device.getModelID());
        //mappingEditor = new ValueMappingForm(valueMappings);
        Button renameDeviceButton = new Button("Rename");
        renameDeviceButton.addClickListener(e -> {
            zigbeeService.renameDevice(device.getFriendlyName(), friendlyName.getValue());
            device.setFriendlyName(friendlyName.getValue());
            Notification.show("Device sucessfully renamed", 5000, Notification.Position.BOTTOM_CENTER);
        });
        add(new HorizontalLayout(friendlyName, renameDeviceButton));
        //add(mappingEditor);
        add(new HorizontalLayout(saveButton, cancelButton));
    }

    private void cancel() {
        getUI().ifPresent(ui -> ui.navigate(MainView.class));
    }

    private void save() {
        //deviceConfiguration.setMapping(device.getManufacturerName(), device.getModelID(), mappingEditor.getMappings());
        zigbeeService.refreshDeviceList();
        getUI().ifPresent(ui -> ui.navigate(MainView.class));
    }

}
