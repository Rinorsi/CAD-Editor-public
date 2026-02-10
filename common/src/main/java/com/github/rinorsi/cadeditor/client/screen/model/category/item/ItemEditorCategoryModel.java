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
}
