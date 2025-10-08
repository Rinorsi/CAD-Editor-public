package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.ActionEntryView;

public class ActionEntryController extends EntryController<ActionEntryModel, ActionEntryView> {
    public ActionEntryController(ActionEntryModel model, ActionEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getButton().setLabel(model.getLabel());
        view.getButton().onAction(model.getAction());
    }
}
