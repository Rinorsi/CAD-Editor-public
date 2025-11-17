package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public abstract class ItemEditorCategoryModel extends EditorCategoryModel {
    protected ItemEditorCategoryModel(Component name, ItemEditorModel parent) {
        super(name, parent);
    }

    @Override
    public ItemEditorModel getParent() {
        return (ItemEditorModel) super.getParent();
    }

    protected CompoundTag getData() {
        return getContext().getTag();
    }

    protected CompoundTag getTag() {
        CompoundTag data = getData();
        return data.getCompound("tag").orElseGet(() -> {
            CompoundTag tag = new CompoundTag();
            data.put("tag", tag);
            return tag;
        });
    }

    protected CompoundTag getSubTag(String name) {
        return getTag().getCompound(name).orElseGet(CompoundTag::new);
    }

    protected CompoundTag getOrCreateTag() {
        return getTag();
    }

    protected CompoundTag getOrCreateSubTag(String name) {
        CompoundTag tag = getOrCreateTag();
        return tag.getCompound(name).orElseGet(() -> {
            CompoundTag child = new CompoundTag();
            tag.put(name, child);
            return child;
        });
    }
}
