package com.github.rinorsi.cadeditor.client.screen.controller.entry;

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
        view.getTextField().textProperty().addListener(model::setValue);
        model.valueProperty().addListener(view.getTextField()::setText);
        view.getTextField().validProperty().addListener(model::setValid);
        view.getTextField().setText(model.getValue());
        String placeholder = model.getPlaceholder();
        if (placeholder != null) {
            view.getTextField().setPlaceholder(Component.literal(placeholder));
        }
    }
}
