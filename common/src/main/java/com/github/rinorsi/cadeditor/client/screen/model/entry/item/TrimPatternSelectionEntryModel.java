package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemTrimCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.SelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public class TrimPatternSelectionEntryModel extends SelectionEntryModel {
    public TrimPatternSelectionEntryModel(ItemTrimCategoryModel category, String value, Consumer<String> action) {
        super(category, ModTexts.TRIM_PATTERN, value, action);
    }

    @Override
    public List<String> getSuggestions() {
        return ClientCache.getTrimPatternSuggestions();
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.TRIM_PATTERN;
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        return ClientCache.getTrimPatternSelectionItems();
    }
}
