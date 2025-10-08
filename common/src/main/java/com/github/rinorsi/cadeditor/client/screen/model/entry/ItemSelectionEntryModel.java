package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public class ItemSelectionEntryModel extends SelectionEntryModel {
    public ItemSelectionEntryModel(CategoryModel category, MutableComponent label, String value, Consumer<String> action) {
        super(category, label, value, action);
    }

    @Override
    public List<String> getSuggestions() {
        return ClientCache.getItemSuggestions();
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.ITEM;
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        return ClientCache.getItemSelectionItems();
    }
}
