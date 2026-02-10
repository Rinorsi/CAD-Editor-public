package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.storage.TagValueInput;

import java.util.Set;

public class ItemSpawnEggCategoryModel extends ItemEditorCategoryModel {
    private static final Set<String> TRANSIENT_ENTITY_TAG_KEYS = Set.of(
            "Pos",
            "Motion",
            "Rotation",
            "UUID",
            "UUIDMost",
            "UUIDLeast"
    );

    private final SpawnEggItem item;
    private CompoundTag spawnData;
    private CompoundTag initialSerializedData = new CompoundTag();
    private CompoundTag initialEditorData = new CompoundTag();
    private EntityEntryModel entityEntry;

    public ItemSpawnEggCategoryModel(ItemEditorModel editor, SpawnEggItem item) {
        super(ModTexts.SPAWN_EGG, editor);
        this.item = item;
        spawnData = readSpawnData(editor.getContext().getItemStack(), editor.getContext().getTag());
    }

    @Override
    protected void setupEntries() {
        var stack = getParent().getContext().getItemStack();
        var registries = ClientUtil.registryAccess();
        CompoundTag editorData = prepareEditorData(spawnData, stack);
        var valueInput = TagValueInput.create(ProblemReporter.DISCARDING, registries, editorData);
        entityEntry = new EntityEntryModel(this,
                EntityType.by(valueInput).orElse(item.getType(stack)),
                editorData,
                value -> {
                });
        getEntries().add(entityEntry.withWeight(0));
        initialSerializedData = spawnData == null ? new CompoundTag() : spawnData.copy();
        initialEditorData = editorData.copy();
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        if (stack == null) {
            return;
        }
        CompoundTag editorValue = sanitizeEntityData(entityEntry == null ? spawnData : entityEntry.copyValue());
        CompoundTag selectedData = editorValue.equals(initialEditorData) ? initialSerializedData.copy() : editorValue.copy();
        CompoundTag sanitizedData = sanitizeSpawnEggComponentPayload(selectedData);
        spawnData = sanitizedData.copy();

        CompoundTag itemData = getData();
        CompoundTag legacyTag = itemData.getCompound("tag").orElse(null);
        if (legacyTag != null && legacyTag.contains("EntityTag")) {
            legacyTag.remove("EntityTag");
            if (legacyTag.isEmpty()) {
                itemData.remove("tag");
            }
        }
        if (sanitizedData.isEmpty()
                || !sanitizedData.contains("id")
                || sanitizedData.getString("id").orElse("").isEmpty()) {
            stack.remove(DataComponents.ENTITY_DATA);
            spawnData = new CompoundTag();
            initialSerializedData = new CompoundTag();
            initialEditorData = new CompoundTag();
            return;
        }
        EntityType<?> entityType = resolveEntityType(stack, sanitizedData);
        CompoundTag componentPayload = sanitizedData.copy();
        componentPayload.remove("id");
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(entityType, componentPayload));
        spawnData = componentPayload.copy();
        spawnData.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
        initialSerializedData = spawnData.copy();
        initialEditorData = editorValue.copy();
    }

    private static CompoundTag readSpawnData(ItemStack stack, CompoundTag rootTag) {
        if (stack != null) {
            TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
            if (data != null) {
                CompoundTag tag = data.copyTagWithoutId();
                tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(data.type()).toString());
                return tag;
            }
        }
        if (rootTag != null) {
            CompoundTag legacy = rootTag.getCompound("tag").orElse(null);
            if (legacy != null) {
                CompoundTag entityTag = legacy.getCompound("EntityTag").orElse(null);
                if (entityTag != null) {
                    return entityTag.copy();
                }
            }
        }
        return new CompoundTag();
    }

    private CompoundTag prepareEditorData(CompoundTag source, ItemStack stack) {
        CompoundTag normalized = sanitizeSpawnEggComponentPayload(source);
        EntityType<?> type = resolveEntityType(stack, normalized);
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            normalized.putString("id", key.toString());
        }
        return normalized;
    }

    private static CompoundTag sanitizeEntityData(CompoundTag source) {
        if (source == null || source.isEmpty()) {
            return new CompoundTag();
        }
        CompoundTag sanitized = source.copy();
        String id = sanitized.getStringOr("id", "").trim();
        if (id.isEmpty()) {
            sanitized.remove("id");
            return sanitized;
        }
        Identifier parsed = ClientUtil.parseResourceLocation(id);
        sanitized.putString("id", parsed == null ? id : parsed.toString());
        return sanitized;
    }

    static CompoundTag sanitizeSpawnEggComponentPayload(CompoundTag source) {
        CompoundTag sanitized = sanitizeEntityData(source);
        if (sanitized.isEmpty()) {
            return sanitized;
        }
        for (String key : TRANSIENT_ENTITY_TAG_KEYS) {
            sanitized.remove(key);
        }
        float health = sanitized.getFloatOr("Health", Float.NaN);
        if (!Float.isNaN(health) && health <= 0f) {
            sanitized.remove("Health");
        }
        return sanitized;
    }

    private EntityType<?> resolveEntityType(ItemStack stack, CompoundTag data) {
        String id = data == null ? "" : data.getString("id").orElse("");
        Identifier parsed = ClientUtil.parseResourceLocation(id);
        if (parsed != null && BuiltInRegistries.ENTITY_TYPE.containsKey(parsed)) {
            return BuiltInRegistries.ENTITY_TYPE.getValue(parsed);
        }
        return item.getType(stack);
    }
}
