package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionFilter;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public abstract class FilteredSelectionEntryModel extends SelectionEntryModel {
    protected FilteredSelectionEntryModel(CategoryModel category, MutableComponent label, String value,
                                          Consumer<String> action) {
        super(category, label, value, action);
    }

    @Override
    public Type getType() {
        return Type.FILTERED_SELECTION;
    }

    public abstract List<ListSelectionFilter> getFilters();

    public String getInitialFilterId() {
        return null;
    }
}
