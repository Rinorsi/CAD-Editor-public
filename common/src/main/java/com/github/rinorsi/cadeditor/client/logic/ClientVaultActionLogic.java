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
        DebugLog.infoKey("cadeditor.debug.vault.give", slot, itemStack.getDisplayName().getString());
        NetworkManager.sendToServer(NetworkManager.GIVE_VAULT_ITEM, new GiveVaultItemPacket(slot, itemStack));
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
            ItemStack stack = ClientUtil.parseItemStack(storedItems.get(i));
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
