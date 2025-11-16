package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("this-escape")
public class EntityEntryModel extends ValueEntryModel<CompoundTag> {
    private final ObjectProperty<EntityType<?>> entityTypeProperty;
    private final ObjectProperty<String> entityIdProperty;

    public EntityEntryModel(CategoryModel category, EntityType<?> entityType, CompoundTag spawnData, Consumer<CompoundTag> action) {
        super(category, ModTexts.ENTITY, sanitizeInitialValue(spawnData, entityType), action);
        entityTypeProperty = ObjectProperty.create(entityType);
        entityIdProperty = ObjectProperty.create("");
        syncFromValue(getValue());
    }

    @Override
    public void apply() {
        setValue(getValue());
        super.apply();
        defaultValue = copyValue();
    }

    @Override
    public void reset() {
        setValue(defaultValue == null ? new CompoundTag() : defaultValue.copy());
    }

    @Override
    public void setValue(CompoundTag value) {
        CompoundTag sanitized = sanitizeIncomingValue(value);
        super.setValue(sanitized);
        syncFromValue(sanitized);
    }

    public void setEntityId(String rawValue) {
        setValue(updateIdCopy(getValue(), rawValue));
    }

    public String getEntityId() {
        return entityIdProperty().getValue();
    }

    public ObjectProperty<String> entityIdProperty() {
        return entityIdProperty;
    }

    public EntityType<?> getEntityType() {
        return entityTypeProperty().getValue();
    }

    public ObjectProperty<EntityType<?>> entityTypeProperty() {
        return entityTypeProperty;
    }

    public CompoundTag copyValue() {
        CompoundTag copy = getValue() == null ? new CompoundTag() : getValue().copy();
        ensureEntityId(copy);
        return copy;
    }

    @Override
    public Type getType() {
        return Type.ENTITY;
    }

    private void syncFromValue(CompoundTag value) {
        String id = value.contains("id", Tag.TAG_STRING) ? value.getString("id") : "";
        EntityType<?> type = findEntityType(id);
        if (type != null) {
            String canonical = Objects.requireNonNull(EntityType.getKey(type)).toString();
            if (!Objects.equals(canonical, id)) {
                value.putString("id", canonical);
                id = canonical;
            }
            entityTypeProperty().setValue(type);
            entityIdProperty().setValue(canonical);
            setValid(true);
        } else {
            entityTypeProperty().setValue(null);
            entityIdProperty().setValue(id);
            setValid(false);
        }
    }

    private static CompoundTag sanitizeInitialValue(CompoundTag spawnData, EntityType<?> entityType) {
        CompoundTag value = spawnData == null ? new CompoundTag() : spawnData.copy();
        if ((value == null || !value.contains("id", Tag.TAG_STRING) || value.getString("id").isEmpty()) && entityType != null) {
            value.putString("id", Objects.requireNonNull(EntityType.getKey(entityType)).toString());
        }
        return value;
    }

    private CompoundTag sanitizeIncomingValue(CompoundTag incoming) {
        CompoundTag value = incoming == null ? new CompoundTag() : incoming.copy();
        String id = value.contains("id", Tag.TAG_STRING) ? value.getString("id") : "";
        EntityType<?> type = findEntityType(id);
        if (type != null) {
            String canonical = Objects.requireNonNull(EntityType.getKey(type)).toString();
            if (!Objects.equals(canonical, id)) {
                value.putString("id", canonical);
            }
        } else if ((id == null || id.isEmpty()) && getEntityType() != null) {
            value.putString("id", Objects.requireNonNull(EntityType.getKey(getEntityType())).toString());
        }
        return value;
    }

    private CompoundTag updateIdCopy(CompoundTag source, String raw) {
        CompoundTag value = source == null ? new CompoundTag() : source.copy();
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            value.remove("id");
            return value;
        }
        ResourceLocation location = ClientUtil.parseResourceLocation(trimmed);
        value.putString("id", location != null ? location.toString() : trimmed);
        return value;
    }

    private static EntityType<?> findEntityType(String value) {
        ResourceLocation location = ClientUtil.parseResourceLocation(value);
        return location == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(location).orElse(null);
    }

    private void ensureEntityId(CompoundTag tag) {
        if (tag == null) {
            return;
        }
        if ((!tag.contains("id", Tag.TAG_STRING) || tag.getString("id").isEmpty()) && getEntityType() != null) {
            tag.putString("id", Objects.requireNonNull(EntityType.getKey(getEntityType())).toString());
        }
    }
}
