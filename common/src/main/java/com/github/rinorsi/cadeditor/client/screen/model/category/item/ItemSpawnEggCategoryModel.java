package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;

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
        CompoundTag editorData = prepareEditorData(spawnData, stack);
        entityEntry = new EntityEntryModel(this,
                EntityType.by(editorData).orElse(item.getType(stack)),
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
        if (itemData.contains("tag", Tag.TAG_COMPOUND)) {
            CompoundTag legacyTag = itemData.getCompound("tag");
            if (legacyTag.contains("EntityTag", Tag.TAG_COMPOUND)) {
                legacyTag.remove("EntityTag");
            }
            if (legacyTag.isEmpty()) {
                itemData.remove("tag");
            }
        }
        if (sanitizedData.isEmpty()
                || !sanitizedData.contains("id", Tag.TAG_STRING)
                || sanitizedData.getString("id").isEmpty()) {
            stack.remove(DataComponents.ENTITY_DATA);
            spawnData = new CompoundTag();
            initialSerializedData = new CompoundTag();
            initialEditorData = new CompoundTag();
            return;
        }
        EntityType<?> entityType = resolveEntityType(stack, sanitizedData);
        CompoundTag componentPayload = sanitizedData.copy();
        // Keep id to preserve non-default entity assignment when command/data is exported.
        componentPayload.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(componentPayload));
        spawnData = componentPayload.copy();
        initialSerializedData = spawnData.copy();
        initialEditorData = editorValue.copy();
    }

    private static CompoundTag readSpawnData(ItemStack stack, CompoundTag rootTag) {
        if (stack != null) {
            CustomData data = stack.get(DataComponents.ENTITY_DATA);
            if (data != null && !data.isEmpty()) {
                return data.copyTag();
            }
        }
        if (rootTag != null && rootTag.contains("tag", Tag.TAG_COMPOUND)) {
            CompoundTag legacy = rootTag.getCompound("tag");
            if (legacy.contains("EntityTag", Tag.TAG_COMPOUND)) {
                return legacy.getCompound("EntityTag").copy();
            }
        }
        return new CompoundTag();
    }

    private CompoundTag prepareEditorData(CompoundTag source, ItemStack stack) {
        CompoundTag normalized = sanitizeSpawnEggComponentPayload(source);
        EntityType<?> type = resolveEntityType(stack, normalized);
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
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
        String id = sanitized.getString("id").trim();
        if (id.isEmpty()) {
            sanitized.remove("id");
            return sanitized;
        }
        ResourceLocation parsed = ClientUtil.parseResourceLocation(id);
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
        float health = sanitized.getFloat("Health");
        if (sanitized.contains("Health", Tag.TAG_FLOAT) && health <= 0f) {
            sanitized.remove("Health");
        }
        return sanitized;
    }

    private EntityType<?> resolveEntityType(ItemStack stack, CompoundTag data) {
        String id = data == null ? "" : data.getString("id");
        ResourceLocation parsed = ClientUtil.parseResourceLocation(id);
        if (parsed != null && BuiltInRegistries.ENTITY_TYPE.containsKey(parsed)) {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(parsed).orElse(item.getType(stack));
        }
        return item.getType(stack);
    }
}
