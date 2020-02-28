package org.msk.loxone.zigbee2mqtt.manager.ui;

import static java.lang.String.format;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.msk.loxone.zigbee2mqtt.zigbee.DeviceConfiguration;


//@Route
//@SpringComponent
@RequiredArgsConstructor
//@UIScope
public class EditMappingForm extends VerticalLayout {

    private final MappingForm mappingForm;
    private final ValueMappingSourceForm valueMappingSourceForm;

    private TextField currentlyEditingValueTextField = new TextField();
    //private Label currentlyEditingValueTextFieldLabel = new Label("");

    private Button createMappingButton = new Button("Create mapping", this::createNewMapping);

    private void createNewMapping(ClickEvent<Button> buttonClickEvent) {
        valueMappingSourceForm.getSelected().ifPresent(i->{
            mappingForm.addMapping(convert(i));
        });
    }

    @PostConstruct
    private void setupUI() throws MqttException {
        add(mappingForm);
        //add(currentlyEditingValueTextFieldLabel);
        add(new HorizontalLayout(new Label("Map to:"), currentlyEditingValueTextField, createMappingButton));

        add(valueMappingSourceForm);
        valueMappingSourceForm.setDoubleclickListener(this::onValueMappingSourceDoubleclick);
        add(new HorizontalLayout(new Button("Save", this::onSave), new Button("Cancel", this::onCancel)));

        //setCurrentlyEditedMapping(null);
    }

    private void onValueMappingSourceDoubleclick(ValueMappingSourceForm.ListeningGridModel model) {
        //setCurrentlyEditedMapping(convert(model));
    }

    private MappingForm.MappingGridModel convert(ValueMappingSourceForm.ListeningGridModel listeningGridModel) {
        return new MappingForm.MappingGridModel(listeningGridModel.getManufacturer(), listeningGridModel.getModelId(), listeningGridModel.getPath(),
                listeningGridModel.getValue(), currentlyEditingValueTextField.getValue());
    }


    /*
    private void setCurrentlyEditedMapping(MappingGridModel item) {
        currentlyEditingMapping = item;
        currentlyEditingValueTextField.setValue("");
        //currentlyEditingValueTextFieldLabel.setText("");
        if (item != null) {
            currentlyEditingValueTextField.setValue(item.getTranslatedValue());
//            currentlyEditingValueTextFieldLabel
//                    .setText(format("Map %s:%s field '%s' value '%s' to:", item.manufacturer, item.modelId, item.path, item.originalValue));
        }
        createMappingButton.setEnabled(item != null);
    }




    private void createNewMapping(ClickEvent<Button> buttonClickEvent) {
        currentlyEditingMapping.setTranslatedValue(currentlyEditingValueTextField.getValue());
        mappingGridModel.remove(currentlyEditingMapping);
        mappingGridModel.add(currentlyEditingMapping);
        updateMappingGrid();
        setCurrentlyEditedMapping(null);
    }
    */


    private void onCancel(ClickEvent<Button> buttonClickEvent) {
        UI.getCurrent().navigate(MainView.class);
    }

    private void onSave(ClickEvent<Button> buttonClickEvent) {
        mappingForm.save();
        UI.getCurrent().navigate(MainView.class);
    }




}
