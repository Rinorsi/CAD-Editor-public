package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.util.SnbtHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

@SuppressWarnings("unused")
public class ItemCustomDataCategoryModel extends ItemEditorCategoryModel {
    private StringEntryModel snbtEntry;

    public ItemCustomDataCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("custom_data"), editor);
    }

    @Override
    protected void setupEntries() {
        var stack = getParent().getContext().getItemStack();
        var existing = stack.get(DataComponents.CUSTOM_DATA);
        String value = existing != null ? existing.copyTag().toString() : "";
        snbtEntry = new StringEntryModel(this, ModTexts.gui("custom_data"), value, v -> {});
        getEntries().add(snbtEntry);
    }

    @Override
    public int getEntryListStart() { return -1; }

    @Override
    public void apply() {
        super.apply();
        String raw = snbtEntry.getValue() == null ? "" : snbtEntry.getValue().trim();
        var stack = getParent().getContext().getItemStack();
        if (raw.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
            snbtEntry.setValid(true);
            return;
        }
        try {
            CompoundTag tag = SnbtHelper.parse(raw);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            snbtEntry.setValid(true);
        } catch (Exception ex) {
            snbtEntry.setValid(false);
        }
    }
}

