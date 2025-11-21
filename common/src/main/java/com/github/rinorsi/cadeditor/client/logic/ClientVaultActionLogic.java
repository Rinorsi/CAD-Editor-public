package com.github.rinorsi.cadeditor.client.logic;

import com.github.rinorsi.cadeditor.client.ClientContext;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.GiveVaultItemPacket;
import com.github.rinorsi.cadeditor.common.network.NetworkManager;
import com.github.rinorsi.cadeditor.mixin.AbstractContainerScreenMixin;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientVaultActionLogic {
    public static void giveVaultItem(int slot, ItemStack itemStack) {
        int serverSlot = slot;
        if (serverSlot >= 36) {
            serverSlot -= 36; // 热键栏在客户端偏移了 36，需要还原成服务器的 0-8
        }
        if (serverSlot < 0) {
            serverSlot = 0;
        } else if (serverSlot >= Inventory.INVENTORY_SIZE) {
            serverSlot = Inventory.INVENTORY_SIZE - 1;
        }
        DebugLog.infoKey("cadeditor.debug.vault.give", serverSlot, itemStack.getDisplayName().getString());
        NetworkManager.sendToServer(NetworkManager.GIVE_VAULT_ITEM, new GiveVaultItemPacket(serverSlot, itemStack));
    }

    /**
     * 将物品直接发到当前选中的热键栏槽位，非创造且服务器没装模组时给出提示。
     */
    public static void giveToSelectedHotbar(ItemStack stack) {
        if (Minecraft.getInstance().player == null) {
            DebugLog.infoKey("cadeditor.debug.vault.no_player");
            return;
        }
        ItemStack copy = stack.copy();
        int targetSlot = Minecraft.getInstance().player.getInventory().selected; // 0-8
        if (ClientContext.isModInstalledOnServer()) {
            DebugLog.infoKey("cadeditor.debug.vault.send_hotbar", targetSlot);
            giveVaultItem(targetSlot, copy);
        } else if (Minecraft.getInstance().player.isCreative()) {
            int slotToUpdate = targetSlot + Inventory.INVENTORY_SIZE;
            Minecraft.getInstance().player.connection.send(new ServerboundSetCreativeModeSlotPacket(slotToUpdate, copy));
            Minecraft.getInstance().player.getInventory().setItem(targetSlot, copy);
            Minecraft.getInstance().player.getInventory().setChanged();
            DebugLog.infoKey("cadeditor.debug.vault.apply_creative_hotbar", targetSlot);
        } else {
            ClientUtil.showMessage(ModTexts.Messages.errorServerModRequired(ModTexts.VAULT));
            DebugLog.infoKey("cadeditor.debug.vault.server_required_hotbar");
        }
    }

    /**
     * 无界面时（空手按 J）快速从保险库取物，目标槽为玩家当前选中的热键栏槽位。
     */
    public static boolean openQuickVaultSelection() {
        DebugLog.infoKey("cadeditor.debug.vault.open_quick");
        if (Minecraft.getInstance().player == null) {
            DebugLog.infoKey("cadeditor.debug.vault.no_player");
            return false;
        }

        List<VaultItemListSelectionElementModel> elements = new ArrayList<>();
        Map<String, ItemStack> stacksById = new LinkedHashMap<>();
        List<CompoundTag> storedItems = Vault.getInstance().getItems();
        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(ClientUtil.registryAccess(), storedItems.get(i));
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cadeditor", "vault_item_hotbar_" + i);
            elements.add(new VaultItemListSelectionElementModel(id, stack));
            stacksById.put(id.toString(), stack.copy());
        }

        if (elements.isEmpty()) {
            DebugLog.infoKey("cadeditor.debug.vault.empty");
            return false;
        }

        int targetSlot = Minecraft.getInstance().player.getInventory().selected; // 0-8
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_item_quick", elements, selectedId -> {
            ItemStack chosen = stacksById.get(selectedId);
            if (chosen == null) {
                return;
            }
            DebugLog.infoKey("cadeditor.debug.vault.choice", chosen.getHoverName().getString());
            giveToSelectedHotbar(chosen);
        });
        return true;
    }

    public static boolean openVaultSelection(AbstractContainerScreen<?> screen) {
        DebugLog.infoKey("cadeditor.debug.vault.open");
        Slot hoveredSlot = ((AbstractContainerScreenMixin) screen).getHoveredSlot();
        if (hoveredSlot == null || !(hoveredSlot.container instanceof Inventory)) {
            DebugLog.infoKey("cadeditor.debug.vault.invalid_slot");
            return false;
        }

        List<VaultItemListSelectionElementModel> elements = new ArrayList<>();
        Map<String, ItemStack> stacksById = new LinkedHashMap<>();
        List<CompoundTag> storedItems = Vault.getInstance().getItems();
        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(ClientUtil.registryAccess(), storedItems.get(i));
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cadeditor", "vault_item_" + i);
            elements.add(new VaultItemListSelectionElementModel(id, stack));
            stacksById.put(id.toString(), stack.copy());
        }

        if (elements.isEmpty()) {
            DebugLog.infoKey("cadeditor.debug.vault.empty");
            return false;
        }

        int slotIndex = hoveredSlot.getContainerSlot();
        if (screen instanceof CreativeModeInventoryScreen creative) {
            if (creative.isInventoryOpen()) {
                slotIndex = ClientUtil.convertCreativeInventorySlot(slotIndex);
            }
        }

        int targetSlot = slotIndex;
        Slot slotRef = hoveredSlot;
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_item", elements, selectedId -> {
            ItemStack chosen = stacksById.get(selectedId);
            if (chosen == null) {
                return;
            }
            DebugLog.infoKey("cadeditor.debug.vault.choice", chosen.getHoverName().getString());
            ItemStack copy = chosen.copy();
            if (ClientContext.isModInstalledOnServer()) {
                DebugLog.infoKey("cadeditor.debug.vault.send", targetSlot);
                giveVaultItem(targetSlot, copy);
            } else if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
                int slotToUpdate = targetSlot + Inventory.INVENTORY_SIZE;
                Minecraft.getInstance().player.connection.send(new ServerboundSetCreativeModeSlotPacket(slotToUpdate, copy));
                slotRef.set(copy);
                slotRef.setChanged();
                DebugLog.infoKey("cadeditor.debug.vault.apply_creative", targetSlot);
            } else {
                ClientUtil.showMessage(ModTexts.Messages.errorServerModRequired(ModTexts.VAULT));
                DebugLog.infoKey("cadeditor.debug.vault.server_required");
            }
        });
        return true;
    }
}
