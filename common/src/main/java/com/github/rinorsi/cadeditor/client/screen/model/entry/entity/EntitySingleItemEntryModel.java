package com.github.rinorsi.cadeditor.client.screen.model.entry.entity;

import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ItemContainerSlotEntryModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class EntitySingleItemEntryModel extends ItemContainerSlotEntryModel {
    private final MutableComponent label;

    public EntitySingleItemEntryModel(EditorCategoryModel category, MutableComponent label, ItemStack stack) {
        super(category, stack);
        this.label = label;
    }

    @Override
    public Component getSlotLabel() {
        return label.copy();
    }

    @Override
    public boolean isDeletable() {
        return false;
    }
}
