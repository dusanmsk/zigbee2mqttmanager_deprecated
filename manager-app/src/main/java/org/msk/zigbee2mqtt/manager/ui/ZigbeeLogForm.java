package org.msk.zigbee2mqtt.manager.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.msk.zigbee2mqtt.MqttService;
import org.msk.zigbee2mqtt.ZigbeeService;
import org.msk.zigbee2mqtt.ui.util.FilteringGrid;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Route
@UIScope
@SpringComponent
@Push
@PreserveOnRefresh
@RequiredArgsConstructor
@Slf4j
public class ZigbeeLogForm extends VerticalLayout {

    private static final int MAX_SIZE = 200;
    private final ZigbeeService zigbeeService;
    private final MqttService mqttService;
    private ObjectMapper objectMapper = new ObjectMapper();
    private FilteringGrid<LogMessage> filteringGrid;
    private TextArea formattedMessageArea;

    @PostConstruct
    void init() {
        filteringGrid = new FilteringGrid<>(LogMessage.class) {
            @Override
            protected boolean doFilter(LogMessage logMessage, String filterText) {
                if (logMessage == null || filterText == null) {
                    return true;
                }
                return logMessage.message.toLowerCase().contains(filterText.toLowerCase());
            }
        };
        filteringGrid.getGrid().addSelectionListener(this::itemSelected);
        formattedMessageArea = new TextArea();
        formattedMessageArea.setSizeFull();

        add(filteringGrid);
        add(new Button("Clear", this::onClearButton));
        add(new Button("Back", this::onBackButton));
        add(formattedMessageArea);

        mqttService.subscribe(zigbeeService.zigbeeTopic("/#"), this::processLogMessage);
    }

    private void onBackButton(ClickEvent<Button> buttonClickEvent) {
        UI.getCurrent().navigate(MainView.class);
    }

    private void onClearButton(ClickEvent<Button> buttonClickEvent) {
        filteringGrid.getModel().clear();
        filteringGrid.refreshGrid();
        formattedMessageArea.setValue("");
    }

    private void processLogMessage(Mqtt5Publish mqtt5Publish) {
        List<LogMessage> model = filteringGrid.getModel();
        model.add(LogMessage.builder()
                .message(new String(mqtt5Publish.getPayloadAsBytes()))
                .topic(mqtt5Publish.getTopic().toString())
                .build());
        if (model.size() > MAX_SIZE) {
            model.remove(0);
        }
        filteringGrid.refreshGrid();
    }

    private void itemSelected(SelectionEvent<Grid<LogMessage>, LogMessage> gridLogMessageSelectionEvent) {
        AtomicReference<String> formattedMessage = new AtomicReference<>("");
        gridLogMessageSelectionEvent.getFirstSelectedItem().ifPresent(item -> {
            try {
                formattedMessage.set(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readValue(item.message, Object.class)));
            } catch (JsonProcessingException e) {
                formattedMessage.set(item.message);
            }
        });
        formattedMessageArea.setValue(formattedMessage.get());
    }

    @Builder
    @Getter
    @AllArgsConstructor
    static public class LogMessage {
        @Builder.Default
        private LocalDateTime timestamp = LocalDateTime.now();
        private String topic;
        private String message;
    }

}
