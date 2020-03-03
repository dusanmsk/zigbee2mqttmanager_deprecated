package org.msk.loxone.zigbee2mqtt.manager.ui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorOpenEvent;
import com.vaadin.flow.component.grid.editor.EditorSaveEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.msk.zigbee2mqtt.ZigbeeDevice;
import org.msk.zigbee2mqtt.ZigbeeService;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

import static java.lang.String.format;


@Route
@SpringComponent
@RequiredArgsConstructor
@UIScope
@Slf4j
@Push
public class MainView extends VerticalLayout {

    private final ZigbeeService zigbeeService;
    private Grid<ZigbeeDevice> zigbeeDeviceGrid;
    private Editor<ZigbeeDevice> editor;
    private TextField friendlyNameTextField;
    private String currentlyEditedOldName = null;
    private Button disableJoinButton = new Button("Disable", this::disableJoin);
    private TextField autoDisableMinutesTextField = new TextField("Auto disable after (min)");
    private Button enableJoinButton = new Button("Enable", this::enableJoin);
    private Label statusLabel = new Label();
    private UI ui;

    @PostConstruct
    public void init() {
        setupUI();
    }

    private void setupUI() {
        ui = UI.getCurrent();
        setupGrid();
        add(statusLabel);
        add(new HorizontalLayout(enableJoinButton, disableJoinButton, autoDisableMinutesTextField));
        add(new Text("Zigbee device list:"));
        add(new Button("Refresh", this::refreshDeviceList));
        add(zigbeeDeviceGrid);
        autoDisableMinutesTextField.setValue("30");
        update();
    }


    private void setupGrid() {
        // setup grid and columns
        zigbeeDeviceGrid = new Grid<>();
        Grid.Column<ZigbeeDevice> friendlyNameColumn = zigbeeDeviceGrid.addColumn(ZigbeeDevice::getFriendlyName).setHeader("Friendly name").setSortable(true);
        zigbeeDeviceGrid.addColumn(ZigbeeDevice::getManufacturerName).setHeader("Manufacturer").setSortable(true);
        zigbeeDeviceGrid.addColumn(ZigbeeDevice::getModelID).setHeader("Model").setSortable(true);
        zigbeeDeviceGrid.addColumn(ZigbeeDevice::getType).setHeader("Type").setSortable(true);
        zigbeeDeviceGrid.addColumn(ZigbeeDevice::getIeeeAddr).setHeader("Address").setSortable(true);
        Grid.Column<ZigbeeDevice> editorColumn = zigbeeDeviceGrid.addComponentColumn(this::createEditButton).setHeader("").setSortable(false);

        // setup rename (editor)
        Binder<ZigbeeDevice> binder = new Binder<>(ZigbeeDevice.class);
        editor = zigbeeDeviceGrid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);
        friendlyNameTextField = new TextField();
        binder.forField(friendlyNameTextField)
                .withValidator(new StringLengthValidator("Friendly name length must be between 1 and 20.", 1, 20))  // // todo dusan.zatkovsky validate with zigbee2mqtt documentation
                .bind("friendlyName");
        friendlyNameColumn.setEditorComponent(friendlyNameTextField);

        Button save = new Button("Save", e -> editor.save());
        save.addClassName("save");

        Button cancel = new Button("Cancel", e -> editor.cancel());
        cancel.addClassName("cancel");

        zigbeeDeviceGrid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);
        editor.addSaveListener(this::onSaveEditing);
        editor.addOpenListener(this::onStartEditingDevice);

    }

    private void onStartEditingDevice(EditorOpenEvent<ZigbeeDevice> zigbeeDeviceEditorOpenEvent) {
        this.currentlyEditedOldName = zigbeeDeviceEditorOpenEvent.getItem().getFriendlyName();
    }

    private void onSaveEditing(EditorSaveEvent<ZigbeeDevice> zigbeeDeviceEditorSaveEvent) {
        if (currentlyEditedOldName != null) {
            String newName = zigbeeDeviceEditorSaveEvent.getItem().getFriendlyName();
            zigbeeService.renameDevice(currentlyEditedOldName, newName);
            Notification.show(format("Device '%s' renamed to '%s'", currentlyEditedOldName, newName));
            currentlyEditedOldName = null;
        }
    }

    private Button createEditButton(ZigbeeDevice device) {
        return new Button("Rename", i -> {
            editor.editItem(device);
            friendlyNameTextField.focus();
        });
    }

    private void refreshDeviceList(ClickEvent<Button> buttonClickEvent) {
        zigbeeDeviceGrid.setItems(zigbeeService.getDeviceList());
    }


    private void enableJoin(ClickEvent<Button> buttonClickEvent) {
        zigbeeService.enableJoin(true, Integer.parseInt(autoDisableMinutesTextField.getValue()) * 60);
    }

    private void disableJoin(ClickEvent<Button> buttonClickEvent) {
        zigbeeService.enableJoin(false, 0);
    }

    @Scheduled(fixedDelay = 5000)
    void update() {
        ui.access(() -> {
            if (zigbeeService.isJoinEnabled()) {
                enableJoinButton.setEnabled(false);
                disableJoinButton.setEnabled(true);
                statusLabel.setText(format("Joining is enabled for next %s minutes", zigbeeService.getJoinTimeout() / 60));
            } else {
                statusLabel.setText("Joining is disabled");
                enableJoinButton.setEnabled(true);
                disableJoinButton.setEnabled(false);

            }
        });

    }

}
