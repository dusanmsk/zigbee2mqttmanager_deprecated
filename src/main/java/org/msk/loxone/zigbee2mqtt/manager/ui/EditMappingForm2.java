package org.msk.loxone.zigbee2mqtt.manager.ui;

import javax.annotation.PostConstruct;

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

@Route
@SpringComponent
@RequiredArgsConstructor
@UIScope
public class EditMappingForm2 extends VerticalLayout {

    private final MappingForm2 mappingForm;
    private final ValueMappingSourceForm2 valueMappingSourceForm;

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
        //add(currentlyEditingValueTextFieldLabel);
        add(new HorizontalLayout(new Label("Map to:"), currentlyEditingValueTextField, createMappingButton));

        add(valueMappingSourceForm);
        add(new HorizontalLayout(new Button("Save", this::onSave), new Button("Cancel", this::onCancel)));

    }

    private void onCancel(ClickEvent<Button> buttonClickEvent) {
        UI.getCurrent().navigate(MainView.class);
    }

    private void onSave(ClickEvent<Button> buttonClickEvent) {
        mappingForm.save();
        UI.getCurrent().navigate(MainView.class);
    }

}
