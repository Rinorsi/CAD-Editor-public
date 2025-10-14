package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.AddListEntryEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.AddListEntryEntryView;

public class AddListEntryEntryController extends EntryController<AddListEntryEntryModel, AddListEntryEntryView> {
    public AddListEntryEntryController(AddListEntryEntryModel model, AddListEntryEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        getView().getButton().getTooltip().add(getModel().getTooltip());
        getView().getButton().onAction(getModel().getAction());
    }
}
