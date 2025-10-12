package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FilteredSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionFilter;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public class SoundEventSelectionEntryModel extends FilteredSelectionEntryModel {
    private final String initialFilterId;

    public SoundEventSelectionEntryModel(CategoryModel category, MutableComponent label, String value,
                                         Consumer<String> action, String initialFilterId) {
        super(category, label, value, action);
        this.initialFilterId = initialFilterId;
    }

    @Override
    public List<String> getSuggestions() {
        return ClientCache.getSoundEventSuggestions();
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.SOUND_EVENT;
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        return ClientCache.getSoundEventSelectionItems();
    }

    @Override
    public List<ListSelectionFilter> getFilters() {
        return ClientCache.getSoundEventFilters();
    }

    @Override
    public String getInitialFilterId() {
        return initialFilterId;
    }
}
