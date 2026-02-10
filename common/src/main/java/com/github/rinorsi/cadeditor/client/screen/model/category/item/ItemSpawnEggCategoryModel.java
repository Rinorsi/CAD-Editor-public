package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public class ItemSpawnEggCategoryModel extends ItemEditorCategoryModel {
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
        CompoundTag editorData = hydrateForEditor(spawnData, stack, registries);
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
        spawnData = selectedData.copy();

        CompoundTag itemData = getData();
        CompoundTag legacyTag = itemData.getCompound("tag").orElse(null);
        if (legacyTag != null && legacyTag.contains("EntityTag")) {
            legacyTag.remove("EntityTag");
            if (legacyTag.isEmpty()) {
                itemData.remove("tag");
            }
        }
        if (spawnData == null || spawnData.isEmpty()
                || !spawnData.contains("id")
                || spawnData.getString("id").orElse("").isEmpty()) {
            stack.remove(DataComponents.ENTITY_DATA);
            spawnData = new CompoundTag();
            initialSerializedData = new CompoundTag();
            initialEditorData = new CompoundTag();
            return;
        }
        EntityType<?> entityType = resolveEntityType(stack, spawnData);
        CompoundTag sanitized = spawnData.copy();
        sanitized.remove("id");
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(entityType, sanitized));
        spawnData = sanitized.copy();
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

    private CompoundTag hydrateForEditor(CompoundTag source, ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag normalized = sanitizeEntityData(source);
        EntityType<?> type = resolveEntityType(stack, normalized);
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            normalized.putString("id", key.toString());
        }
        if (!shouldHydrate(normalized)) {
            return normalized;
        }
        CompoundTag hydrated = createDefaultEntityData(type, registries);
        if (hydrated.isEmpty()) {
            return normalized;
        }
        hydrated.putString("id", normalized.getStringOr("id", ""));
        return hydrated;
    }

    private static boolean shouldHydrate(CompoundTag data) {
        return data != null && data.contains("id") && data.size() <= 1;
    }

    private static CompoundTag createDefaultEntityData(EntityType<?> type, HolderLookup.Provider registries) {
        if (type == null) {
            return new CompoundTag();
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            return new CompoundTag();
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) {
            return new CompoundTag();
        }
        try {
            CompoundTag minimal = new CompoundTag();
            minimal.putString("id", key.toString());
            var input = TagValueInput.create(ProblemReporter.DISCARDING, registries, minimal);
            var entity = EntityType.create(input, minecraft.level, EntitySpawnReason.LOAD).orElse(null);
            if (entity == null) {
                return minimal;
            }
            TagValueOutput writer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
            entity.saveWithoutId(writer);
            CompoundTag hydrated = writer.buildResult();
            hydrated.putString("id", key.toString());
            return hydrated;
        } catch (Exception ignored) {
            return new CompoundTag();
        }
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
        ResourceLocation parsed = ClientUtil.parseResourceLocation(id);
        sanitized.putString("id", parsed == null ? id : parsed.toString());
        return sanitized;
    }

    private EntityType<?> resolveEntityType(ItemStack stack, CompoundTag data) {
        String id = data == null ? "" : data.getString("id").orElse("");
        ResourceLocation parsed = ClientUtil.parseResourceLocation(id);
        if (parsed != null && BuiltInRegistries.ENTITY_TYPE.containsKey(parsed)) {
            return BuiltInRegistries.ENTITY_TYPE.getValue(parsed);
        }
        return item.getType(stack);
    }
}
