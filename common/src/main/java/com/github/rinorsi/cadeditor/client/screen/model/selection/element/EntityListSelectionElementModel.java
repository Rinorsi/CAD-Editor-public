package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.Locale;

public class EntityListSelectionElementModel extends ItemListSelectionElementModel {
    private final Component displayName;
    private final String displayNameLowercase;

    public EntityListSelectionElementModel(EntityType<?> entityType, ResourceLocation id) {
        super(entityType.getDescriptionId(), id, buildIcon(entityType));
        this.displayName = entityType.getDescription().copy();
        this.displayNameLowercase = displayName.getString().toLowerCase(Locale.ROOT);
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
        String lower = s.toLowerCase(Locale.ROOT);
        if (displayNameLowercase.contains(lower)) {
            return true;
        }
        return super.matches(s);
    }

    private static ItemStack buildIcon(EntityType<?> type) {
        SpawnEggItem egg = SpawnEggItem.byId(type);
        return egg != null ? new ItemStack(egg) : ItemStack.EMPTY;
    }
}
