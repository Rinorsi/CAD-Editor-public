package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

import java.util.Locale;

public class TrimMaterialSelectionElementModel extends ItemListSelectionElementModel {
    private final Component displayName;
    private final String searchText;

    public TrimMaterialSelectionElementModel(Component displayName, Identifier id, ItemStack icon) {
        super(displayName.getString(), id, icon);
        this.displayName = displayName;
        this.searchText = displayName.getString().toLowerCase(Locale.ROOT);
    }

    public TrimMaterialSelectionElementModel(Component displayName, Identifier id, Supplier<ItemStack> iconSupplier) {
        super(displayName.getString(), id, iconSupplier);
        this.displayName = displayName;
        this.searchText = displayName.getString().toLowerCase(Locale.ROOT);
    }

    @Override
    public Component getDisplayName() {
        return displayName.copy();
    }

    @Override
    public boolean matches(String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }
        if (super.matches(query)) {
            return true;
        }
        String lower = query.toLowerCase(Locale.ROOT);
        return searchText.contains(lower);
    }
}
