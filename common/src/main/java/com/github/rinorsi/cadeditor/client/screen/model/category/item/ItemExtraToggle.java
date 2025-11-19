package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

public enum ItemExtraToggle {
    CUSTOM_MODEL_DATA(ModTexts.gui("custom_model_data")),
    EQUIPPABLE(ModTexts.gui("equippable_settings")),
    TOOL(ModTexts.TOOL),
    ENCHANTMENTS(ModTexts.ENCHANTMENTS),
    FOOD(ModTexts.gui("food")),
    CONSUMABLE(ModTexts.gui("consumable_settings")),
    ATTRIBUTE_MODIFIERS(ModTexts.ATTRIBUTE_MODIFIERS),
    DEATH_PROTECTION(ModTexts.gui("death_protection"));

    private final MutableComponent label;

    ItemExtraToggle(MutableComponent label) {
        this.label = label.copy();
    }

    public MutableComponent coloredLabel() {
        return label.copy();
    }
}
