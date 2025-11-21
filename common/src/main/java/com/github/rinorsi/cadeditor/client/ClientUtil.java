package com.github.rinorsi.cadeditor.client;

import com.github.rinorsi.cadeditor.common.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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
        int selected = inv.selected;
        if (selected >= 0 && selected < Inventory.INVENTORY_SIZE) {
            ItemStack selectedStack = inv.getItem(selected);
            if (selectedStack.isEmpty() || (ItemStack.isSameItemSameComponents(selectedStack, stack) && selectedStack.getCount() < selectedStack.getMaxStackSize())) {
                return selected < 9 ? selected + 36 : selected;
            }
        }

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
