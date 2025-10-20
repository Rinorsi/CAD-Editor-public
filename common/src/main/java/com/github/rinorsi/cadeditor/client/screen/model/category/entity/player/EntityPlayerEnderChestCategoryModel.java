package com.github.rinorsi.cadeditor.client.screen.model.category.entity.player;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.PlayerInventorySlotEntryModel;
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
 * Provides editing for player ender chest contents (EnderItems).
 */
public class EntityPlayerEnderChestCategoryModel extends EntityCategoryModel {
    private static final String ENDER_ITEMS_TAG = "EnderItems";
    private static final int ENDER_SLOT_COUNT = 27;

    private final List<PlayerInventorySlotEntryModel> slotEntries = new ArrayList<>();

    public EntityPlayerEnderChestCategoryModel(EntityEditorModel editor) {
        super(Component.translatable("cadeditor.gui.ender_chest"), editor);
    }

    @Override
    protected void setupEntries() {
        slotEntries.clear();
        List<ItemStack> slots = readEnderSlots();
        for (int slot = 0; slot < slots.size(); slot++) {
            MutableComponent label = Component.translatable("cadeditor.gui.ender_chest.slot", slot + 1);
            PlayerInventorySlotEntryModel entry = new PlayerInventorySlotEntryModel(this, label, slot, slots.get(slot));
            slotEntries.add(entry);
            getEntries().add(entry);
        }
    }

    @Override
    public void apply() {
        super.apply();
        writeEnderItems();
        syncPlayerInstance();
    }

    private List<ItemStack> readEnderSlots() {
        List<ItemStack> slots = new ArrayList<>(Collections.nCopies(ENDER_SLOT_COUNT, ItemStack.EMPTY));
        CompoundTag data = getData();
        if (data == null) {
            return slots;
        }
        ListTag list = data.getList(ENDER_ITEMS_TAG, Tag.TAG_COMPOUND);
        for (Tag element : list) {
            if (!(element instanceof CompoundTag compound)) {
                continue;
            }
            int slot = Byte.toUnsignedInt(compound.getByte("Slot"));
            if (slot >= 0 && slot < ENDER_SLOT_COUNT) {
                slots.set(slot, ItemStack.parseOptional(ClientUtil.registryAccess(), compound.copy()));
            }
        }
        return slots;
    }

    private void writeEnderItems() {
        CompoundTag data = ensurePlayerTag();
        ListTag list = new ListTag();
        for (PlayerInventorySlotEntryModel entry : slotEntries) {
            ItemStack stack = entry.getItemStack();
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag tag = (CompoundTag) stack.save(ClientUtil.registryAccess(), new CompoundTag());
            tag.putByte("Slot", (byte) entry.getSlotId());
            list.add(tag);
        }
        if (!list.isEmpty()) {
            data.put(ENDER_ITEMS_TAG, list);
        } else {
            data.remove(ENDER_ITEMS_TAG);
        }
    }

    private void syncPlayerInstance() {
        Player player = resolvePlayerEntity();
        if (player == null) {
            return;
        }
        for (PlayerInventorySlotEntryModel entry : slotEntries) {
            player.getEnderChestInventory().setItem(entry.getSlotId(), entry.getItemStack().copy());
        }
    }

    private Player resolvePlayerEntity() {
        return getEntity() instanceof Player player ? player : null;
    }

    private CompoundTag ensurePlayerTag() {
        CompoundTag data = getData();
        if (data == null) {
            data = new CompoundTag();
            getContext().setTag(data);
        }
        return data;
    }
}
