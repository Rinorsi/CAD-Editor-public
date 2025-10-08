package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.LabeledEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.LabeledEntryView;

public abstract class LabeledEntryController<M extends LabeledEntryModel, V extends LabeledEntryView> extends EntryController<M, V> {
    public LabeledEntryController(M model, V view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getLabel().labelProperty().bind(model.labelProperty());
        if (view.getLabel().getParent() == view.getRoot()) {
            int labelWeight = model.getLabelWeight();
            if (labelWeight <= 0) {
                view.getRoot().getChildren().remove(view.getLabel());
            } else {
                view.getRoot().setWeight(view.getLabel(), labelWeight);
            }
        }
    }
}
