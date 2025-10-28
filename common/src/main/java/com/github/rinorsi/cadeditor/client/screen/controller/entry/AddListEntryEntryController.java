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
        int entryHeight = getModel().getCategory().getEntryHeight();
        int buttonHeight = Math.min(entryHeight, 32);
        getView().getRoot().setMinHeight(entryHeight);
        getView().getRoot().setPrefHeight(entryHeight);
        getView().getContentContainer().setPrefHeight(buttonHeight);
        getView().getContentContainer().setMinHeight(buttonHeight);
        getView().getButton().getTooltip().add(getModel().getTooltip());
        getView().getButton().onAction(getModel().getAction());
    }
}
