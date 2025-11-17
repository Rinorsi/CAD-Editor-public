package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.franckyi.guapi.api.mvc.AbstractController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EntryView;

public abstract class EntryController<M extends EntryModel, V extends EntryView> extends AbstractController<M, V> {
    public EntryController(M model, V view) {
        super(model, view);
    }

    @Override
    public void bind() {
        view.getUpButton().onAction(() -> model.getCategory().moveEntryUp(model.getListIndex()));
        view.getDownButton().onAction(() -> model.getCategory().moveEntryDown(model.getListIndex()));
        if (model.isDeletable()) {
            view.getDeleteButton().onAction(() -> model.getCategory().deleteEntry(model.getListIndex()));
        } else {
            view.disableDeleteButton();
        }
        view.getResetButton().setVisible(model.isResetable());
        view.getResetButton().onAction(this::resetModel);
        view.getUpButton().disableProperty().bind(model.listIndexProperty().eq(0));
        view.getDownButton().disableProperty().bind(model.listIndexProperty().eq(model.listSizeProperty().substract(1)));
        model.listIndexProperty().addListener(this::updateListButtons);
        model.reorderableProperty().addListener(value -> updateListButtons(model.getListIndex()));
        updateListButtons(model.getListIndex());
    }

    private void updateListButtons(int listIndex) {
        view.setListButtonsVisible(model.isReorderable() && listIndex >= 0);
    }

    protected void resetModel() {
        model.reset();
    }
}
