package org.msk.zigbee2mqtt;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;

public class UiUtils {

    public static Button createButton(String title, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(title);
        button.addClickListener(listener);
        return button;
    }

}
