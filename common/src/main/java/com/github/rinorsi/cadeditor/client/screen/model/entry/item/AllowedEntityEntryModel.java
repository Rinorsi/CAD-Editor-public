package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FilteredSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionFilter;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AllowedEntityEntryModel extends FilteredSelectionEntryModel {

    public AllowedEntityEntryModel(CategoryModel category, String value, Consumer<String> action) {
        super(category, ModTexts.ENTITY, value, action);
    }

    @Override
    public List<String> getSuggestions() {
        return ClientCache.getEntitySuggestions();
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.ENTITY;
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        return ClientCache.getEntitySelectionItems();
    }

    @Override
    public List<ListSelectionFilter> getFilters() {
        return Collections.emptyList();
    }
}
