package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.SelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RaritySelectionEntryModel extends SelectionEntryModel {
    private static final List<String> VALUES = List.of("common", "uncommon", "rare", "epic");

    public RaritySelectionEntryModel(CategoryModel category, MutableComponent label, String value, Consumer<String> action) {
        super(category, label, normalize(value), action);
    }

    private static String normalize(String v) {
        if (v == null || v.isEmpty()) return "common";
        String s = v.contains(":") ? v.substring(v.indexOf(':') + 1) : v;
        s = s.toLowerCase();
        return VALUES.contains(s) ? s : "common";
    }

    @Override
    public void setValue(String value) {
        super.setValue(normalize(value));
    }

    @Override
    public List<String> getSuggestions() {
        return VALUES;
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.gui("rarity");
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        List<ListSelectionElementModel> list = new ArrayList<>();
        for (String s : VALUES) {
            String key = "cadeditor.gui.rarity." + s;
            list.add(new ListSelectionElementModel(key, net.minecraft.resources.ResourceLocation.parse("minecraft:" + s)));
        }
        return list;
    }
}
