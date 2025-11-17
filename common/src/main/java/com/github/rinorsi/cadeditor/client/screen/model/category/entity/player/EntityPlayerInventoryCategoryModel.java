package com.github.rinorsi.cadeditor.client.screen.model.category.entity.player;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.PlayerInventorySlotEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides editing for player main inventory (slots 0-35).
 */
public class EntityPlayerInventoryCategoryModel extends EntityCategoryModel {
    private static final String INVENTORY_TAG = "Inventory";
    private static final int INVENTORY_SIZE = 36;

    private final List<PlayerInventorySlotEntryModel> slotEntries = new ArrayList<>();
    private final List<CompoundTag> inventoryExtras = new ArrayList<>();

    public EntityPlayerInventoryCategoryModel(EntityEditorModel editor) {
        super(Component.translatable("cadeditor.gui.player_inventory"), editor);
    }

    @Override
    protected void setupEntries() {
        slotEntries.clear();
        inventoryExtras.clear();
        List<ItemStack> slots = readInventorySlots();
        for (int slot = 0; slot < slots.size(); slot++) {
            Component label = slotLabel(slot);
            PlayerInventorySlotEntryModel entry = new PlayerInventorySlotEntryModel(this, label, slot, slots.get(slot));
            slotEntries.add(entry);
            getEntries().add(entry);
        }
    }

    @Override
    public void apply() {
        super.apply();
        writeInventory();
        syncPlayerInstance();
    }

    private List<ItemStack> readInventorySlots() {
        List<ItemStack> slots = new ArrayList<>(Collections.nCopies(INVENTORY_SIZE, ItemStack.EMPTY));
        CompoundTag data = getData();
        if (data == null) {
            return slots;
        }
        ListTag inventory = NbtHelper.getListOrEmpty(data, INVENTORY_TAG);
        for (int i = 0; i < inventory.size(); i++) {
            Tag element = inventory.get(i);
            if (!(element instanceof CompoundTag compound)) continue;
            int slot = Byte.toUnsignedInt(NbtHelper.getByte(compound, "Slot", (byte) -1));
            if (slot >= 0 && slot < INVENTORY_SIZE) {
                ItemStack stack = ClientUtil.parseItemStack(ClientUtil.registryAccess(), compound.copy());
                slots.set(slot, stack);
            } else {
                inventoryExtras.add(compound.copy());
            }
        }
        return slots;
    }

    private void writeInventory() {
        CompoundTag data = ensurePlayerTag();
        ListTag list = new ListTag();
        // Preserve extra slots (armor/offhand legacy entries)
        for (CompoundTag extra : inventoryExtras) {
            list.add(extra.copy());
        }
        for (PlayerInventorySlotEntryModel entry : slotEntries) {
            ItemStack stack = entry.getItemStack();
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag tag = ClientUtil.saveItemStack(ClientUtil.registryAccess(), stack);
            tag.putByte("Slot", (byte) entry.getSlotId());
            list.add(tag);
        }
        data.put(INVENTORY_TAG, list);
    }

    private CompoundTag ensurePlayerTag() {
        CompoundTag data = getData();
        if (data == null) {
            data = new CompoundTag();
            getContext().setTag(data);
        }
        return data;
    }

    private void syncPlayerInstance() {
        Player player = resolvePlayerEntity();
        if (player == null) {
            return;
        }
        for (PlayerInventorySlotEntryModel entry : slotEntries) {
            player.getInventory().setItem(entry.getSlotId(), entry.getItemStack().copy());
        }
    }

    private Player resolvePlayerEntity() {
        return getEntity() instanceof Player player ? player : null;
    }

    private static MutableComponent slotLabel(int slot) {
        if (slot < 9) {
            return Component.translatable("cadeditor.gui.player_inventory.hotbar", slot + 1);
        }
        int index = slot - 9;
        return Component.translatable("cadeditor.gui.player_inventory.main", index + 1);
    }
}
