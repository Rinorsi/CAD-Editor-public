package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public class ItemSpawnEggCategoryModel extends ItemEditorCategoryModel {
    private final SpawnEggItem item;
    private CompoundTag spawnData;

    public ItemSpawnEggCategoryModel(ItemEditorModel editor, SpawnEggItem item) {
        super(ModTexts.SPAWN_EGG, editor);
        this.item = item;
        spawnData = readSpawnData(editor.getContext().getItemStack(), item);
    }

    @Override
    protected void setupEntries() {
        var stack = getParent().getContext().getItemStack();
        var registries = ClientUtil.registryAccess();
        var valueInput = TagValueInput.create(ProblemReporter.DISCARDING, registries, spawnData);
        EntityEntryModel entityEntry = new EntityEntryModel(this,
                EntityType.by(valueInput).orElse(item.getType(registries, stack)),
                spawnData,
                this::setEntity);
        getEntries().add(entityEntry.withWeight(0));
        spawnData = entityEntry.copyValue();
    }

    private void setEntity(CompoundTag compoundTag) {
        this.spawnData = compoundTag == null ? new CompoundTag() : compoundTag.copy();
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        if (stack == null) {
            return;
        }
        if (spawnData == null || spawnData.isEmpty()
                || !spawnData.contains("id")
                || spawnData.getString("id").orElse("").isEmpty()) {
            stack.remove(DataComponents.ENTITY_DATA);
            spawnData = new CompoundTag();
            return;
        }
        CompoundTag sanitized = spawnData.copy();
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(sanitized));
        spawnData = sanitized;
    }

    private static CompoundTag readSpawnData(ItemStack stack, SpawnEggItem spawnEggItem) {
        CompoundTag tag = new CompoundTag();
        if (stack != null) {
            CustomData data = stack.get(DataComponents.ENTITY_DATA);
            if (data != null && !data.isEmpty()) {
                tag = data.copyTag();
            }
        }
        return enrichSpawnData(tag, spawnEggItem, stack);
    }

    private static CompoundTag enrichSpawnData(CompoundTag source, SpawnEggItem spawnEggItem, ItemStack stack) {
        CompoundTag tag = source == null ? new CompoundTag() : source.copy();
        var registries = ClientUtil.registryAccess();
        EntityType<?> fallbackType = spawnEggItem.getType(registries, stack);
        if (!tag.contains("id")) {
            tag.putString("id", EntityType.getKey(fallbackType).toString());
        }
        if (isSparseSpawnData(tag)) {
            CompoundTag hydrated = buildDefaultEntityData(tag.getStringOr("id", ""));
            if (!hydrated.isEmpty()) {
                for (String key : tag.keySet()) {
                    if (tag.get(key) != null) {
                        hydrated.put(key, tag.get(key).copy());
                    }
                }
                tag = hydrated;
            }
        }
        return tag;
    }

    private static boolean isSparseSpawnData(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return true;
        }
        if (!tag.contains("id")) {
            return true;
        }
        return tag.keySet().size() <= 1;
    }

    private static CompoundTag buildDefaultEntityData(String id) {
        if (id == null || id.isBlank() || Minecraft.getInstance().level == null) {
            return new CompoundTag();
        }
        CompoundTag seed = new CompoundTag();
        seed.putString("id", id);
        var registries = ClientUtil.registryAccess();
        var input = TagValueInput.create(ProblemReporter.DISCARDING, registries, seed);
        Entity entity = EntityType.create(input, Minecraft.getInstance().level, EntitySpawnReason.LOAD).orElse(null);
        if (entity == null) {
            return new CompoundTag();
        }
        TagValueOutput writer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        if (!entity.save(writer)) {
            writer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
            entity.saveWithoutId(writer);
        }
        CompoundTag output = writer.buildResult();
        if (output == null) {
            output = new CompoundTag();
        }
        output.putString("id", id);
        stripRuntimeOnlyKeys(output);
        return output;
    }

    private static void stripRuntimeOnlyKeys(CompoundTag tag) {
        tag.remove("UUID");
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Rotation");
        tag.remove("PortalCooldown");
        tag.remove("OnGround");
        tag.remove("FallDistance");
    }
}
