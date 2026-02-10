package com.github.rinorsi.cadeditor.client;

import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ClientUtil {
    public static void showMessage(Component component) {
        CommonUtil.showMessage(Minecraft.getInstance().player, component);
    }

    public static int convertCreativeInventorySlot(int slot) {
        if (slot == 45) {
            slot = 40;
        } else if (slot >= 36) {
            slot %= 36;
        } else if (slot < 9) {
            slot = 36 + 8 - slot;
        }
        return slot;
    }

    public static int findSlot(ItemStack stack) {
        Inventory inv = Minecraft.getInstance().player.getInventory();
        int slot = inv.getSlotWithRemainingSpace(stack);
        if (slot == -1) {
            slot = inv.getFreeSlot();
        }
        if (slot != -1 && slot < 9) {
            slot += 36;
        }
        return slot;
    }

    public static HolderLookup.Provider registryAccess() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            if (minecraft.level != null) {
                return minecraft.level.registryAccess();
            }
            if (minecraft.getConnection() != null) {
                return minecraft.getConnection().registryAccess();
            }
            return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static ItemStack parseItemStack(CompoundTag tag) {
        return parseItemStack(registryAccess(), tag);
    }

    public static ItemStack parseItemStack(HolderLookup.Provider lookup, CompoundTag tag) {
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, lookup);
        return ItemStack.OPTIONAL_CODEC.parse(ops, tag).result().orElse(ItemStack.EMPTY);
    }

    public static CompoundTag saveItemStack(ItemStack stack) {
        return saveItemStack(registryAccess(), stack);
    }

    public static CompoundTag saveItemStack(HolderLookup.Provider lookup, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", BuiltInRegistries.ITEM.getKey((stack == null ? Items.AIR : stack.getItem())).toString());
            tag.putInt("count", stack == null ? 0 : stack.getCount());
            return tag;
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, lookup);
        return ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack)
                .result()
                .filter(result -> result instanceof CompoundTag)
                .map(result -> (CompoundTag) result)
                .orElseGet(() -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                    tag.putInt("count", stack.getCount());
                    return tag;
                });
    }

    public static ResourceLocation parseResourceLocation(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null && !value.contains(":")) {
            location = ResourceLocation.tryParse("minecraft:" + value);
        }
        return location;
    }
}
