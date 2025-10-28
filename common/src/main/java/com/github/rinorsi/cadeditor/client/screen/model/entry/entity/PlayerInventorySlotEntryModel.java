package com.github.rinorsi.cadeditor.client.screen.model.entry.entity;

import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ItemContainerSlotEntryModel;
import net.minecraft.network.chat.Component;

/**
 * Specialized inventory slot entry for player inventories with explicit slot id and label.
 */
public class PlayerInventorySlotEntryModel extends ItemContainerSlotEntryModel {
    private final int slotId;
    private final Component slotLabel;

    public PlayerInventorySlotEntryModel(EditorCategoryModel category, Component slotLabel, int slotId, net.minecraft.world.item.ItemStack stack) {
        super(category, stack);
        this.slotId = slotId;
        this.slotLabel = slotLabel;
    }

    public int getSlotId() {
        return slotId;
    }

    @Override
    public Component getSlotLabel() {
        return slotLabel;
    }
}
