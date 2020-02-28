package org.msk.loxone.zigbee2mqtt.manager.ui;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeDevice;
import org.msk.loxone.zigbee2mqtt.zigbee.ZigbeeService;

@Route
@RequiredArgsConstructor
@UIScope
@SpringComponent
public class EditDeviceForm extends VerticalLayout {

    private final ZigbeeService zigbeeService;
    private final DeviceConfiguration deviceConfiguration;
    private final MainView mainView;

    private TextField friendlyName = new TextField("Friendly name:");
    private ZigbeeDevice device;

    @PostConstruct
    public void init() {
        this.device = mainView.getSelectedDevice().get();
        friendlyName.setValue(device.getFriendlyName());
        add(new HorizontalLayout(friendlyName, new Button("Rename device", this::onRenameDeviceButton)));
        add(new HorizontalLayout(new Button("Back", this::onBackButton)));
    }

    private void onBackButton(ClickEvent<Button> buttonClickEvent) {
        getUI().ifPresent(ui -> ui.navigate(MainView.class));
    }

    private void onRenameDeviceButton(ClickEvent<Button> buttonClickEvent) {
        zigbeeService.renameDevice(device.getFriendlyName(), friendlyName.getValue());
        zigbeeService.refreshDeviceList();
        device.setFriendlyName(friendlyName.getValue());
        Notification.show("Device sucessfully renamed", 5000, Notification.Position.BOTTOM_CENTER);
    }

}
