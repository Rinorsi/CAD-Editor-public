package com.github.rinorsi.cadeditor.client.logic;

import com.github.rinorsi.cadeditor.client.ClientContext;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.GiveVaultItemPacket;
import com.github.rinorsi.cadeditor.common.network.NetworkManager;
import com.github.rinorsi.cadeditor.mixin.AbstractContainerScreenMixin;
import com.github.rinorsi.cadeditor.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientVaultActionLogic {
    public static void giveVaultItem(int slot, ItemStack itemStack) {
        int normalizedSlot = normalizeInventorySlot(slot);
        DebugLog.infoKey("cadeditor.debug.vault.give", normalizedSlot, itemStack.getDisplayName().getString());
        NetworkManager.sendToServer(NetworkManager.GIVE_VAULT_ITEM, new GiveVaultItemPacket(normalizedSlot, itemStack));
    }

    public static void giveToSelectedHotbar(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        Inventory inventory = player.getInventory();
        ItemStack copy = stack.copy();
        int selected = ((InventoryAccessor) inventory).cadeditor$getSelectedSlot();
        if (ClientContext.isModInstalledOnServer()) {
            DebugLog.infoKey("cadeditor.debug.vault.send", selected);
            giveVaultItem(selected, copy);
            return;
        }
        if (!player.isCreative()) {
            ClientUtil.showMessage(ModTexts.Messages.errorServerModRequired(ModTexts.VAULT));
            DebugLog.infoKey("cadeditor.debug.vault.server_required");
            return;
        }
        int creativeSlot = selected + Inventory.INVENTORY_SIZE;
        mc.player.connection.send(new ServerboundSetCreativeModeSlotPacket(creativeSlot, copy));
        ((InventoryAccessor) inventory).cadeditor$getItems().set(selected, copy);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        CommonUtil.showVaultItemGiveSuccess(player);
        DebugLog.infoKey("cadeditor.debug.vault.apply_creative", selected);
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
            Identifier id = Identifier.fromNamespaceAndPath("cadeditor", "vault_item_" + i);
            elements.add(new VaultItemListSelectionElementModel(id, stack));
            stacksById.put(id.toString(), stack.copy());
        }

        if (elements.isEmpty()) {
            DebugLog.infoKey("cadeditor.debug.vault.empty");
            return false;
        }

        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_item", elements, selectedId -> {
            ItemStack chosen = stacksById.get(selectedId);
            if (chosen == null) {
                return;
            }
            DebugLog.infoKey("cadeditor.debug.vault.choice", chosen.getHoverName().getString());
            giveToSelectedHotbar(chosen);
        });
        return true;
    }

    private static int normalizeInventorySlot(int slot) {
        int corrected = slot;
        if (corrected >= Inventory.INVENTORY_SIZE) {
            corrected -= Inventory.INVENTORY_SIZE;
        }
        if (corrected < 0) {
            corrected = 0;
        } else if (corrected >= Inventory.INVENTORY_SIZE) {
            corrected = Inventory.INVENTORY_SIZE - 1;
        }
        return corrected;
    }
}
