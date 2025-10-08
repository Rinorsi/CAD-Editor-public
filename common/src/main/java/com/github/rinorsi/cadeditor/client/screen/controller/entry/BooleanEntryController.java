package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.BooleanEntryView;

import java.util.Objects;

public class BooleanEntryController<M extends BooleanEntryModel, V extends BooleanEntryView> extends ValueEntryController<M, V> {
    public BooleanEntryController(M model, V view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getCheckBox().setChecked(model.getValue());
        view.getCheckBox().checkedProperty().addListener(value -> {
            if (Objects.equals(value, model.getValue())) {
                return;
            }
            model.setValue(value);
            model.apply();
        });
        model.valueProperty().addListener(view.getCheckBox()::setChecked);
    }
}
