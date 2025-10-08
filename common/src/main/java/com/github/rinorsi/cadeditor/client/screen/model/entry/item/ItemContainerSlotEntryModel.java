package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

@SuppressWarnings("this-escape")
public class ItemContainerSlotEntryModel extends EntryModel {
    private final EditorCategoryModel category;
    private ItemStack defaultStack;
    private final ObjectProperty<ItemStack> stackProperty;

    public ItemContainerSlotEntryModel(EditorCategoryModel category, ItemStack stack) {
        super(category);
        this.category = category;
        this.defaultStack = sanitize(stack);
        this.stackProperty = ObjectProperty.create(this.defaultStack.copy());
        this.stackProperty.addListener(value -> setValid(true));
    }

    private ItemStack sanitize(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        if (copy.getCount() <= 0) {
            copy.setCount(1);
        }
        return copy;
    }

    public EditorCategoryModel getContainerCategory() {
        return category;
    }

    public Component getSlotLabel() {
        int index = Math.max(0, getListIndex());
        return Component.literal(String.format("#%02d", index + 1));
    }

    public ObjectProperty<ItemStack> itemStackProperty() {
        return stackProperty;
    }

    public ItemStack getItemStack() {
        ItemStack current = stackProperty.getValue();
        return current == null ? ItemStack.EMPTY : current;
    }

    public void setItemStack(ItemStack stack) {
        ItemStack sanitized = sanitize(stack);
        if (!Objects.equals(stackProperty.getValue(), sanitized)) {
            stackProperty.setValue(sanitized);
        }
    }

    @Override
    public void reset() {
        setItemStack(defaultStack.copy());
    }

    @Override
    public void apply() {
        defaultStack = getItemStack().copy();
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_SLOT;
    }
}
