package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FilteredSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.SelectionEntryView;

public class FilteredSelectionEntryController<M extends FilteredSelectionEntryModel>
        extends SelectionEntryController<M, SelectionEntryView> {
    public FilteredSelectionEntryController(M model, SelectionEntryView view) {
        super(model, view);
    }

    @Override
    protected void openSelectionScreen() {
        String value = model.getValue();
        String namespacedValue = value.contains(":") ? value : "minecraft:" + value;
        ModScreenHandler.openListSelectionScreen(model.getSelectionScreenTitle(), namespacedValue,
                model.getSelectionItems(), model::setValue, model.getFilters(), model.getInitialFilterId());
    }
}
