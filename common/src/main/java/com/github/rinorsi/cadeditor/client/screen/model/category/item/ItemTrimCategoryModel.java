
package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.TrimMaterialSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.TrimPatternSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

import java.util.Optional;

public class ItemTrimCategoryModel extends ItemEditorCategoryModel {
    private String patternId = "";
    private String materialId = "";

    private TrimPatternSelectionEntryModel patternEntry;
    private TrimMaterialSelectionEntryModel materialEntry;

    public ItemTrimCategoryModel(ItemEditorModel editor) {
        super(ModTexts.TRIM, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim != null) {
            patternId = trim.pattern().unwrapKey()
                    .map(ResourceKey::identifier)
                    .map(Identifier::toString)
                    .orElse("");
            materialId = trim.material().unwrapKey()
                    .map(ResourceKey::identifier)
                    .map(Identifier::toString)
                    .orElse("");
        }
        patternEntry = new TrimPatternSelectionEntryModel(this, patternId,
                value -> patternId = value == null ? "" : value.trim());
        materialEntry = new TrimMaterialSelectionEntryModel(this, materialId,
                value -> materialId = value == null ? "" : value.trim());
        patternEntry.setPlaceholder("minecraft:coast");
        materialEntry.setPlaceholder("minecraft:diamond");
        //TODO 记得加上中文搜索和常用排序
        getEntries().add(patternEntry);
        getEntries().add(materialEntry);
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        HolderLookup.RegistryLookup<TrimPattern> patternLookup = ClientUtil.registryAccess()
                .lookup(Registries.TRIM_PATTERN)
                .orElse(null);
        HolderLookup.RegistryLookup<TrimMaterial> materialLookup = ClientUtil.registryAccess()
                .lookup(Registries.TRIM_MATERIAL)
                .orElse(null);
        Optional<Holder.Reference<TrimPattern>> patternHolder = resolvePattern(patternLookup, patternId);
        Optional<Holder.Reference<TrimMaterial>> materialHolder = resolveMaterial(materialLookup, materialId);
        if (patternHolder.isEmpty() || materialHolder.isEmpty()) {
            patternEntry.setValid(patternHolder.isPresent());
            materialEntry.setValid(materialHolder.isPresent());
            return;
        }
        patternEntry.setValid(true);
        materialEntry.setValid(true);
        ArmorTrim trim = new ArmorTrim(materialHolder.get(), patternHolder.get());
        stack.set(DataComponents.TRIM, trim);
        CompoundTag data = getData();
        if (data != null) {
            CompoundTag components = data.getCompound("components").orElse(null);
            if (components != null) {
                components.remove("minecraft:trim");
                if (components.isEmpty()) {
                    data.remove("components");
                }
            }
        }
    }

    private Optional<Holder.Reference<TrimPattern>> resolvePattern(HolderLookup.RegistryLookup<TrimPattern> lookup, String id) {
        if (lookup == null) {
            return Optional.empty();
        }
        Identifier rl = id.isBlank() ? null : Identifier.tryParse(id);
        if (rl == null) {
            return Optional.empty();
        }
        return lookup.get(ResourceKey.create(Registries.TRIM_PATTERN, rl));
    }

    private Optional<Holder.Reference<TrimMaterial>> resolveMaterial(HolderLookup.RegistryLookup<TrimMaterial> lookup, String id) {
        if (lookup == null) {
            return Optional.empty();
        }
        Identifier rl = id.isBlank() ? null : Identifier.tryParse(id);
        if (rl == null) {
            return Optional.empty();
        }
        return lookup.get(ResourceKey.create(Registries.TRIM_MATERIAL, rl));
    }
}
