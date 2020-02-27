package org.msk.loxone.zigbee2mqtt.manager.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@Route("/device/rules")
@SpringComponent
@UIScope
public class DeviceRuleForm extends FormLayout {

    private TextField manufacturerName = new TextField("Manufacturer name:");
    private TextField modelId = new TextField("Model ID:");

    private Button saveButton = new Button("Save");
    private Button cancelButton = new Button("Cancel");

    public DeviceRuleForm() {
        add(manufacturerName, modelId, saveButton, cancelButton);
    }
}
