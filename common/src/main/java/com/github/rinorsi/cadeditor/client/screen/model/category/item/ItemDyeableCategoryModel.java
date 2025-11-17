package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ArmorColorEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class ItemDyeableCategoryModel extends ItemEditorCategoryModel {
    public ItemDyeableCategoryModel(ItemEditorModel editor) {
        super(ModTexts.ARMOR_COLOR, editor);
    }

    @Override
    protected void setupEntries() {
        getEntries().add(new ArmorColorEntryModel(this, getColor(), this::setColor));
    }

    private int getColor() {
        DyedItemColor dyed = getStack().get(DataComponents.DYED_COLOR);
        return dyed != null ? dyed.rgb() : Color.NONE;
    }

    private void setColor(int value) {
        ItemStack stack = getStack();
        if (value == Color.NONE) {
            stack.remove(DataComponents.DYED_COLOR);
        } else {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(value));
        }
    }

    private ItemStack getStack() {
        return getParent().getContext().getItemStack();
    }
}
