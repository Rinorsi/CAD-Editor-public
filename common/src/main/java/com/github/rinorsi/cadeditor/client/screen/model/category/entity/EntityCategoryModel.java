package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public abstract class EntityCategoryModel extends EditorCategoryModel {
    public EntityCategoryModel(Component name, EntityEditorModel editor) {
        super(name, editor);
    }

    @Override
    public EntityEditorContext getContext() {
        return (EntityEditorContext) super.getContext();
    }

    protected CompoundTag getData() {
        return getContext().getTag();
    }

    protected Entity getEntity() {
        return getContext().getEntity();
    }
}
