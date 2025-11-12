package com.github.rinorsi.cadeditor.client.screen.model.entry.entity;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.SelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public class VillagerTypeSelectionEntryModel extends SelectionEntryModel {
    public VillagerTypeSelectionEntryModel(CategoryModel category, MutableComponent label, String value, Consumer<String> action) {
        super(category, label, value, action);
    }

    @Override
    public List<String> getSuggestions() {
        return ClientCache.getVillagerTypeSuggestions();
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.VILLAGER_TYPE;
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        return ClientCache.getVillagerTypeSelectionItems();
    }
}
