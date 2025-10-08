package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.EnumEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EnumEntryView;

public class EnumEntryController<E> extends ValueEntryController<EnumEntryModel<E>, EnumEntryView<E>> {
    public EnumEntryController(EnumEntryModel<E> model, EnumEntryView<E> view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getButton().getValues().setAll(model.getValues());
        if (model.getTextFactory() != null) {
            view.getButton().setTextFactory(model.getTextFactory());
        }
        view.getButton().valueProperty().addListener(model::setValue);
        model.valueProperty().addListener(view.getButton()::setValue);
        view.getButton().setValue(model.getValue());
    }
}
