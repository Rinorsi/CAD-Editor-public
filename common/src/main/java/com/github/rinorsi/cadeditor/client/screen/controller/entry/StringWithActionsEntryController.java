package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.StringWithActionsEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.StringWithActionsEntryView;
import net.minecraft.network.chat.Component;

public class StringWithActionsEntryController extends ValueEntryController<StringWithActionsEntryModel, StringWithActionsEntryView> {
    public StringWithActionsEntryController(StringWithActionsEntryModel model, StringWithActionsEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getTextField().setText(model.getValue());
        view.getTextField().textProperty().addListener(model::setValue);
        model.valueProperty().addListener(value -> {
            String safe = value == null ? "" : value;
            if (!safe.equals(view.getTextField().getText())) {
                view.getTextField().setText(safe);
            }
        });
        view.getTextField().validProperty().addListener(model::setValid);
        String placeholder = model.getPlaceholder();
        if (placeholder != null) {
            view.getTextField().setPlaceholder(Component.literal(placeholder));
        }
        for (var button : model.getButtons()) {
            view.addButton(button.icon(), button.tooltip(), button.action());
        }
    }
}
