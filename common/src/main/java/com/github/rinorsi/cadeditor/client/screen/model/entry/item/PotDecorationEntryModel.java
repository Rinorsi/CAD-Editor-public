package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PotDecorationEntryModel extends ItemContainerSlotEntryModel {
    private final MutableComponent label;

    public PotDecorationEntryModel(EditorCategoryModel category, MutableComponent label, ItemStack stack) {
        super(category, stack);
        this.label = label;
    }

    @Override
    public Component getSlotLabel() {
        return label;
    }

    @Override
    public void setItemStack(ItemStack stack) {
        ItemStack sanitized = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!isAllowedDecoration(sanitized)) {
            ClientUtil.showMessage(ModTexts.Messages.potDecorationInvalid());
            return;
        }
        super.setItemStack(stack);
    }

    private boolean isAllowedDecoration(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (stack.is(ItemTags.DECORATED_POT_SHERDS)) {
            return true;
        }
        return stack.is(Items.BRICK);
    }
}
