package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

public enum ItemExtraToggle {
    FOOD(ModTexts.gui("food")),
    EQUIPPABLE(ModTexts.gui("equippable_settings")),
    CUSTOM_MODEL_DATA(ModTexts.gui("custom_model_data")),
    ATTRIBUTE_MODIFIERS(ModTexts.ATTRIBUTE_MODIFIERS),
    TOOL(ModTexts.TOOL),
    ENCHANTMENTS(ModTexts.ENCHANTMENTS);

    private final MutableComponent label;

    ItemExtraToggle(MutableComponent label) {
        this.label = label.copy();
    }

    public MutableComponent coloredLabel() {
        return label.copy();
    }
}
