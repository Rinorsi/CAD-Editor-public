package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.ReadOnlyStringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.StringEntryView;
import net.minecraft.network.chat.Component;

public class StringEntryController<M extends StringEntryModel, V extends StringEntryView> extends ValueEntryController<M, V> {
    public StringEntryController(M model, V view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        model.valueProperty().addListener(view.getTextField()::setText);
        view.getTextField().validProperty().addListener(model::setValid);
        view.getTextField().setText(model.getValue());
        String placeholder = model.getPlaceholder();
        if (placeholder != null) {
            view.getTextField().setPlaceholder(Component.literal(placeholder));
        }
        boolean readOnly = model instanceof ReadOnlyStringEntryModel;
        view.getTextField().textProperty().addListener(value -> {
            if (readOnly) {
                String current = model.getValue();
                if (current == null) {
                    current = "";
                }
                if (!current.equals(value)) {
                    view.getTextField().setText(current);
                }
            } else {
                model.setValue(value);
            }
        });
        if (readOnly) {
            view.getResetButton().setVisible(false);
        }
    }
}
