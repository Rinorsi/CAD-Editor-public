package com.github.rinorsi.cadeditor.client.screen.model.entry.vault;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.ObservableObjectValue;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.screen.model.category.vault.VaultEntityCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.TagValueInput;

public class VaultEntityEntryModel extends EntryModel {
    private final ObjectProperty<CompoundTag> tagProperty;
    private final ObservableObjectValue<Entity> entityProperty;

    public VaultEntityEntryModel(VaultEntityCategoryModel parent, CompoundTag tag) {
        super(parent);
        tagProperty = ObjectProperty.create(tag);
        entityProperty = tagProperty.map(tag1 -> {
            var level = Minecraft.getInstance().level;
            if (tag1 == null || level == null) {
                return null;
            }
            var input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag1);
            return EntityType.create(input, level, EntitySpawnReason.LOAD).orElse(null);
        });
    }

    public CompoundTag getData() {
        return tagProperty().getValue();
    }

    public ObjectProperty<CompoundTag> tagProperty() {
        return tagProperty;
    }

    public void setData(CompoundTag value) {
        tagProperty().setValue(value);
    }

    public Entity getEntity() {
        return entityProperty().getValue();
    }

    public ObservableObjectValue<Entity> entityProperty() {
        return entityProperty;
    }

    @Override
    public void apply() {
        Vault.getInstance().saveEntity(getData());
    }

    @Override
    public Type getType() {
        return Type.VAULT_ENTITY;
    }
}
