package org.msk.zigbee2mqtt.ui.util;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;

import java.util.ArrayList;
import java.util.List;

public abstract class FilteringGrid<T> extends VerticalLayout {

    private Grid<T> grid;
    private List<T> model = new ArrayList<>();
    private ListDataProvider<T> dataProvider = new ListDataProvider<>(model);

    public FilteringGrid(Class<T> tClass) {
        grid = new Grid<>(tClass);
        grid.setDataProvider(dataProvider);
        TextField filterTextField = new TextField();
        filterTextField.setValueChangeMode(ValueChangeMode.EAGER);
        filterTextField.addValueChangeListener(e -> {
            String filter = e.getValue();
            if (filter.trim().isEmpty()) {
                filter = null;
            }
            String finalFilter = filter;
            dataProvider.setFilter(new SerializablePredicate<T>() {
                @Override
                public boolean test(T t) {
                    return doFilter(t, finalFilter);
                }
            });
            refreshGrid();
        });

        add(new HorizontalLayout(new Label("Filter:"), filterTextField));
        add(grid);
    }

    public List<T> getModel() {
        return model;
    }

    public void refreshGrid() {
        getUI().ifPresent(ui -> ui.access(() -> {
            dataProvider.refreshAll();
        }));
    }

    public Grid<T> getGrid() {
        return grid;
    }

    abstract protected  boolean doFilter(T t, String filterText);
}
