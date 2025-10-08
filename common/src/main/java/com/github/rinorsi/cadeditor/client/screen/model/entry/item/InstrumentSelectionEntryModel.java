package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemInstrumentCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.SelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public class InstrumentSelectionEntryModel extends SelectionEntryModel {
    public InstrumentSelectionEntryModel(ItemInstrumentCategoryModel category, String value, Consumer<String> action) {
        super(category, ModTexts.INSTRUMENT, value, action);
    }

    @Override
    public List<String> getSuggestions() {
        return ClientCache.getInstrumentSuggestions();
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.INSTRUMENT;
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        return ClientCache.getInstrumentSelectionItems();
    }
}
