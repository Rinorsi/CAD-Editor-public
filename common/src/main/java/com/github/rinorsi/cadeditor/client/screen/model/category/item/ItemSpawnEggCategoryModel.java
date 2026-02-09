package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueInput;

public class ItemSpawnEggCategoryModel extends ItemEditorCategoryModel {
    private final SpawnEggItem item;
    private CompoundTag spawnData;

    public ItemSpawnEggCategoryModel(ItemEditorModel editor, SpawnEggItem item) {
        super(ModTexts.SPAWN_EGG, editor);
        this.item = item;
        spawnData = readSpawnData(editor.getContext().getItemStack(), editor.getContext().getTag());
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
            return;
        }
        CompoundTag sanitized = spawnData.copy();
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(sanitized));
        spawnData = sanitized;
    }

    private static CompoundTag readSpawnData(ItemStack stack, CompoundTag rootTag) {
        if (stack != null) {
            CustomData data = stack.get(DataComponents.ENTITY_DATA);
            if (data != null && !data.isEmpty()) {
                return data.copyTag();
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
}
