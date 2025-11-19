package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemExtraComponentsCategoryModel extends ItemEditorCategoryModel {
    private static final ItemExtraToggle[] DISPLAY_ORDER = new ItemExtraToggle[]{
            ItemExtraToggle.EQUIPPABLE,
            ItemExtraToggle.TOOL,
            ItemExtraToggle.ENCHANTMENTS,
            ItemExtraToggle.FOOD,
            ItemExtraToggle.CONSUMABLE,
            ItemExtraToggle.ATTRIBUTE_MODIFIERS,
            ItemExtraToggle.DEATH_PROTECTION,
            ItemExtraToggle.CUSTOM_MODEL_DATA
    };

    private final Map<ItemExtraToggle, BooleanEntryModel> toggleEntries = new LinkedHashMap<>();

    public ItemExtraComponentsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("extra_components").copy().withStyle(ChatFormatting.GREEN), editor);
    }

    @Override
    protected void setupEntries() {
        toggleEntries.clear();
        for (ItemExtraToggle toggle : DISPLAY_ORDER) {
            boolean enabled = getParent().isExtraComponentEnabled(toggle);
            BooleanEntryModel entry = new BooleanEntryModel(
                    this,
                    toggle.coloredLabel(),
                    enabled,
                    value -> getParent().setExtraComponentEnabled(toggle, value != null && value)
            ).withWeight(2);
            toggleEntries.put(toggle, entry);
            getEntries().add(entry);
        }
    }

    public void setToggleValue(ItemExtraToggle key, boolean enabled) {
        BooleanEntryModel entry = toggleEntries.get(key);
        if (entry != null && entry.getValue() != enabled) {
            entry.setValue(enabled);
            entry.apply();
        }
    }

    public BooleanEntryModel getEntry(ItemExtraToggle key) {
        return toggleEntries.get(key);
    }

    @Override
    public void apply() {
        for (Map.Entry<ItemExtraToggle, BooleanEntryModel> entry : toggleEntries.entrySet()) {
            entry.getValue().apply();
        }
    }
}
