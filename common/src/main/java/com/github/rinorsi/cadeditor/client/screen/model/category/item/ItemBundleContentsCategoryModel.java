package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ItemContainerSlotEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

public class ItemBundleContentsCategoryModel extends ItemEditorCategoryModel {
    public ItemBundleContentsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.BUNDLE_CONTENTS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents != null && !contents.isEmpty()) {
            contents.items().forEach(item -> getEntries().add(new ItemContainerSlotEntryModel(this, item.copy())));
        } else {
            getEntries().add(new ItemContainerSlotEntryModel(this, ItemStack.EMPTY));
        }
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
        ItemStack stack = getParent().getContext().getItemStack();
        BundleContents existing = stack.get(DataComponents.BUNDLE_CONTENTS);
        BundleContents.Mutable mutable = new BundleContents.Mutable(existing != null ? existing : BundleContents.EMPTY).clearItems();

        boolean hasInvalid = false;
        for (EntryModel entry : getEntries()) {
            if (entry instanceof ItemContainerSlotEntryModel slotEntry) {
                ItemStack value = slotEntry.getItemStack();
                if (value.isEmpty()) {
                    slotEntry.setValid(true);
                    continue;
                }
                ItemStack copy = value.copy();
                int expected = copy.getCount();
                int inserted = mutable.tryInsert(copy);
                if (inserted != expected) {
                    slotEntry.setValid(false);
                    hasInvalid = true;
                } else {
                    slotEntry.setValid(true);
                }
            }
        }
        if (hasInvalid) {
            return;
        }

        BundleContents applied = mutable.toImmutable();
        if (applied.isEmpty()) {
            stack.remove(DataComponents.BUNDLE_CONTENTS);
        } else {
            stack.set(DataComponents.BUNDLE_CONTENTS, applied);
        }
        cleanComponentTag();
    }

    private void cleanComponentTag() {
        CompoundTag data = getData();
        if (data == null) return;
        CompoundTag components = data.getCompound("components").orElse(null);
        if (components == null) return;
        components.remove("minecraft:bundle_contents");
        if (components.isEmpty()) {
            data.remove("components");
        }
    }
}
