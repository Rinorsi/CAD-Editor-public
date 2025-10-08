package com.github.rinorsi.cadeditor.client.screen.model.category.block;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.BlockEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ItemContainerSlotEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlockContainerGridCategoryModel extends BlockEditorCategoryModel {
    public BlockContainerGridCategoryModel(BlockEditorModel parent) {
        super(ModTexts.CONTAINER_GRID, parent);
    }

    @Override
    protected void setupEntries() {
        CompoundTag tag = getData();
        if (tag == null) return;
        List<ItemStack> slots = readItemSlots(tag);
        if (slots.isEmpty()) {
            getEntries().add(new ItemContainerSlotEntryModel(this, ItemStack.EMPTY));
            return;
        }
        for (ItemStack st : slots) {
            getEntries().add(new ItemContainerSlotEntryModel(this, st));
        }
    }

    private List<ItemStack> readItemSlots(CompoundTag tag) {
        List<ItemStack> out = new ArrayList<>();
        if (!tag.contains("Items", Tag.TAG_LIST)) return out;
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        int maxSlot = -1;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag it = list.getCompound(i);
            int slot = it.contains("Slot", Tag.TAG_BYTE) ? Byte.toUnsignedInt(it.getByte("Slot")) : i;
            if (slot > maxSlot) maxSlot = slot;
        }
        if (maxSlot < 0) return out;
        for (int i = 0; i <= maxSlot; i++) out.add(ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag it = list.getCompound(i);
            int slot = it.contains("Slot", Tag.TAG_BYTE) ? Byte.toUnsignedInt(it.getByte("Slot")) : i;
            ItemStack parsed = ItemStack.parseOptional(ClientUtil.registryAccess(), it);
            if (slot >= 0 && slot < out.size()) {
                out.set(slot, parsed);
            } else {
                out.add(parsed);
            }
        }
        return out;
    }

    @Override
    public boolean canAddEntryInList() {
        return true;
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    public EntryModel createNewListEntry() {
        return new ItemContainerSlotEntryModel(this, ItemStack.EMPTY);
    }

    @Override
    public void apply() {
        super.apply();
        CompoundTag tag = getData();
        if (tag == null) return;
        ListTag items = new ListTag();
        int idx = 0;
        for (EntryModel model : getEntries()) {
            if (!(model instanceof ItemContainerSlotEntryModel entry)) continue;
            ItemStack stack = entry.getItemStack();
            if (stack.isEmpty()) { idx++; continue; }
            CompoundTag itemTag = (CompoundTag) stack.save(ClientUtil.registryAccess(), new CompoundTag());
            itemTag.putByte("Slot", (byte) idx);
            items.add(itemTag);
            idx++;
        }
        if (items.isEmpty()) {
            tag.remove("Items");
        } else {
            tag.put("Items", items);
        }
    }
}
