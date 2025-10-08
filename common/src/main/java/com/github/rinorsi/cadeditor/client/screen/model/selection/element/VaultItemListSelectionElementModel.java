package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class VaultItemListSelectionElementModel extends ItemListSelectionElementModel {
    private final Component displayName;
    private final String hoverNameLowercase;

    public VaultItemListSelectionElementModel(ResourceLocation id, ItemStack stack) {
        super(stack.getDescriptionId(), id, stack.copy());
        this.displayName = stack.getHoverName().copy();
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
}
