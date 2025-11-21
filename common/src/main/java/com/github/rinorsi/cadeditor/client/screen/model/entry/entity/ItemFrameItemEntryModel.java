package com.github.rinorsi.cadeditor.client.screen.model.entry.entity;

import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ItemContainerSlotEntryModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Reuses the standard container slot entry UI for a single item-frame stack,
 * but exposes a custom label instead of the auto-generated slot index.
 */
public class ItemFrameItemEntryModel extends ItemContainerSlotEntryModel {
    private final Component label;

    public ItemFrameItemEntryModel(EditorCategoryModel category, ItemStack stack, Component label) {
        super(category, stack);
        this.label = label;
    }

    @Override
    public Component getSlotLabel() {
        return label;
    }
}
