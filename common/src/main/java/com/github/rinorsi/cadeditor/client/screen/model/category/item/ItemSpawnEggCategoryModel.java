package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;

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
        EntityEntryModel entityEntry = new EntityEntryModel(this,
                EntityType.by(spawnData).orElse(item.getType(stack)),
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
        if (spawnData == null || spawnData.isEmpty()
                || !spawnData.contains("id", Tag.TAG_STRING)
                || spawnData.getString("id").isEmpty()) {
            if (itemData.contains("tag", Tag.TAG_COMPOUND)) {
                CompoundTag tag = itemData.getCompound("tag");
                tag.remove("EntityTag");
                if (tag.isEmpty()) {
                    itemData.remove("tag");
                }
            }
            stack.remove(DataComponents.ENTITY_DATA);
            spawnData = new CompoundTag();
            return;
        }
        CompoundTag sanitized = spawnData.copy();
        getOrCreateTag().put("EntityTag", sanitized.copy());
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
        if (rootTag != null && rootTag.contains("tag", Tag.TAG_COMPOUND)) {
            CompoundTag legacy = rootTag.getCompound("tag");
            if (legacy.contains("EntityTag", Tag.TAG_COMPOUND)) {
                return legacy.getCompound("EntityTag").copy();
            }
        }
        return new CompoundTag();
    }
}
