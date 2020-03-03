package org.msk.loxone.zigbee2mqtt.manager.ui;

import javax.annotation.PostConstruct;

import com.vaadin.flow.component.notification.Notification;
import lombok.RequiredArgsConstructor;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.msk.zigbee2mqtt.configuration.DeviceConfiguration;

@Route
@SpringComponent
@RequiredArgsConstructor
@UIScope
@Slf4j
public class EditMappingForm extends VerticalLayout {

    private final MappingForm mappingForm;
    private final ValueMappingSourceForm valueMappingSourceForm;
    private final DeviceConfiguration deviceConfiguration;

    private TextField currentlyEditingValueTextField = new TextField();
    private Button createMappingButton = new Button("Create mapping", this::createNewMapping);

    private void createNewMapping(ClickEvent<Button> buttonClickEvent) {
        valueMappingSourceForm.getSelected().stream().findFirst().ifPresent(i -> {
            i.setTranslatedValue(currentlyEditingValueTextField.getValue());
            mappingForm.addMapping(i);
            currentlyEditingValueTextField.setValue("");
        });
    }

    @PostConstruct
    private void setupUI() {
        add(mappingForm);
        add(new HorizontalLayout(new Label("Map to:"), currentlyEditingValueTextField, createMappingButton));

        add(valueMappingSourceForm);
        add(new HorizontalLayout(new Button("Save", this::onSave)));

    }

    private void onSave(ClickEvent<Button> buttonClickEvent) {
        try {
            mappingForm.commit();
            deviceConfiguration.save();
            UI.getCurrent().navigate(MainView.class);
        } catch ( Exception e) {
            log.error("Failed to save config", e);
            Notification.show("Failed to save config");
        }
    }

}
