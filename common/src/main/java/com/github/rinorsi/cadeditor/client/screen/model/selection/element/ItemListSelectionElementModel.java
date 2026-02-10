package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Supplier;

public class ItemListSelectionElementModel extends ListSelectionElementModel {
    private final Supplier<ItemStack> itemSupplier;

    public ItemListSelectionElementModel(String name, Identifier id, Supplier<ItemStack> itemSupplier) {
        super(name, id);
        Objects.requireNonNull(itemSupplier, "itemSupplier");
        this.itemSupplier = () -> {
            ItemStack stack = itemSupplier.get();
            return stack == null ? ItemStack.EMPTY : stack;
        };
    }

    public ItemListSelectionElementModel(String name, Identifier id, ItemStack itemStack) {
        this(name, id, () -> itemStack == null ? ItemStack.EMPTY : itemStack.copy());
    }

    public ItemStack getItem() {
        return itemSupplier.get();
    }

    @Override
    public Type getType() {
        return Type.ITEM;
    }
}
