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
        if (data == null) {
            return null;
        }
        return data.getCompound("tag").orElse(null);
    }

    protected CompoundTag ensureTag() {
        CompoundTag data = getData();
        if (data == null) {
            return new CompoundTag();
        }
        return data.getCompound("tag").orElseGet(() -> {
            CompoundTag tag = new CompoundTag();
            data.put("tag", tag);
            return tag;
        });
    }

    protected CompoundTag getSubTag(String name) {
        CompoundTag tag = getTag();
        if (tag == null) {
            return new CompoundTag();
        }
        return tag.getCompound(name).orElseGet(CompoundTag::new);
    }

    protected CompoundTag getOrCreateTag() {
        return ensureTag();
    }

    protected CompoundTag getOrCreateSubTag(String name) {
        CompoundTag tag = ensureTag();
        return tag.getCompound(name).orElseGet(() -> {
            CompoundTag child = new CompoundTag();
            tag.put(name, child);
            return child;
        });
    }
}
