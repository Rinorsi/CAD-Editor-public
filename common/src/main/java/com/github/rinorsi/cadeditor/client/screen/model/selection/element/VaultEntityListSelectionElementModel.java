package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import java.util.Locale;

/**
 * List selection element representing an entity stored in the vault.
 */
public class VaultEntityListSelectionElementModel extends ItemListSelectionElementModel {
    private final Component displayName;
    private final String searchLabel;

    public VaultEntityListSelectionElementModel(ResourceLocation id, CompoundTag tag) {
        super(buildName(tag), id, () -> buildIcon(tag));
        this.searchLabel = buildName(tag);
        this.displayName = Component.literal(searchLabel);
    }

    private static String buildName(CompoundTag tag) {
        String id = tag.getString("id");
        if (id.isEmpty()) {
            id = "minecraft:unknown";
        }
        String prettyName = EntityType.byString(id)
                .map(type -> type.getDescription().getString())
                .orElse(id);
        return String.format(Locale.ROOT, "%s (%s)", prettyName, id);
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
        return searchLabel.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)) || super.matches(s);
    }

    private static ItemStack buildIcon(CompoundTag tag) {
        String id = tag.getString("id");
        if (!id.isEmpty()) {
            SpawnEggItem egg = EntityType.byString(id).map(SpawnEggItem::byId).orElse(null);
            if (egg != null) {
                return new ItemStack(egg);
            }
        }
        return new ItemStack(Items.SPAWNER);
    }
}
