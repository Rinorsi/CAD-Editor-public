package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class VaultItemListSelectionElementModel extends ItemListSelectionElementModel {
    private final Component displayName;
    private final String hoverNameLowercase;

    public VaultItemListSelectionElementModel(Identifier id, ItemStack stack) {
        this(id, stack.copy(), stack.getHoverName().copy());
    }

    private VaultItemListSelectionElementModel(Identifier id, ItemStack copy, Component displayName) {
        super(resolveBaseId(copy), id, () -> copy.copy());
        this.displayName = displayName;
        this.hoverNameLowercase = displayName.getString().toLowerCase(Locale.ROOT);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public boolean matches(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        if (hoverNameLowercase.contains(s.toLowerCase(Locale.ROOT))) {
            return true;
        }
        return super.matches(s);
    }

    private static String resolveBaseId(ItemStack stack) {
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null ? "minecraft:air" : key.toString();
    }
}
