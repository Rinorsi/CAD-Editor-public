package com.github.rinorsi.cadeditor.common;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class ColoredItemHelper {
    private ColoredItemHelper() {
    }

    public static ItemStack createColoredPotionItem(ResourceLocation potionId, int color) {
        ItemStack stack = new ItemStack(Items.POTION);
        var lookupOpt = registryAccess().lookup(Registries.POTION);
        if (lookupOpt.isPresent()) {
            var lookup = lookupOpt.get();
            ResourceLocation rl = potionId == null ? ResourceLocation.parse("minecraft:empty") : potionId;
            ResourceKey<Potion> key = ResourceKey.create(Registries.POTION, rl);
            Holder<Potion> holder = lookup.get(key).orElse(null);
            PotionContents contents;
            java.util.List<net.minecraft.world.effect.MobEffectInstance> effects = java.util.List.of();
            if (holder != null) {
                contents = new PotionContents(java.util.Optional.of(holder),
                        color != Color.NONE ? java.util.Optional.of(color) : java.util.Optional.empty(),
                        effects,
                        java.util.Optional.empty());
            } else {
                contents = new PotionContents(java.util.Optional.empty(),
                        color != Color.NONE ? java.util.Optional.of(color) : java.util.Optional.empty(),
                        effects,
                        java.util.Optional.empty());
            }
            stack.set(DataComponents.POTION_CONTENTS, contents);
            return stack;
        }
        // Fallback legacy path
        CompoundTag data = new CompoundTag();
        CompoundTag tag = new CompoundTag();
        tag.putString("Potion", potionId == null ? "minecraft:empty" : potionId.toString());
        if (color != Color.NONE) {
            tag.putInt("CustomPotionColor", color);
        }
        data.putString("id", "minecraft:potion");
        data.putInt("Count", 1);
        data.put("tag", tag);
        return ItemStack.parseOptional(registryAccess(), data);
    }

    public static ItemStack createColoredArmorItem(ItemStack armorItem, int color) {
        ItemStack copy = armorItem.copy();
        if (color == Color.NONE) {
            copy.remove(DataComponents.DYED_COLOR);
        } else {
            copy.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
        }
        return copy;
    }

    public static ItemStack createFireworkStarItem(int color) {
        ItemStack stack = new ItemStack(Items.FIREWORK_STAR);
        if (color == Color.NONE) {
            stack.remove(DataComponents.FIREWORK_EXPLOSION);
            return stack;
        }
        IntArrayList colors = new IntArrayList();
        colors.add(color);
        FireworkExplosion explosion = new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, colors, new IntArrayList(), false, false);
        stack.set(DataComponents.FIREWORK_EXPLOSION, explosion);
        return stack;
    }

    private static HolderLookup.Provider registryAccess() {
        return ClientUtil.registryAccess();
    }
}
