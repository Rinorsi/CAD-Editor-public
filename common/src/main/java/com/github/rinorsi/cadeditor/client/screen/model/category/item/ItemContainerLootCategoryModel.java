package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.LootTableSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;

@SuppressWarnings("unused")
public class ItemContainerLootCategoryModel extends ItemEditorCategoryModel {
    private LootTableSelectionEntryModel tableIdEntry;
    private StringEntryModel seedEntry;

    public ItemContainerLootCategoryModel(ItemEditorModel parent) {
        super(ModTexts.CONTAINER_LOOT, parent);
    }

    @Override
    protected void setupEntries() {
        String tableId = "";
        String seed = "";
        CompoundTag data = getData();
        //TODO 后面要接上战利品表生成器，最好还能一键回填
        if (data != null && data.contains("components", Tag.TAG_COMPOUND)) {
            CompoundTag components = data.getCompound("components");
            if (components.contains("minecraft:container_loot", Tag.TAG_COMPOUND)) {
                CompoundTag loot = components.getCompound("minecraft:container_loot");
                if (loot.contains("loot_table", Tag.TAG_STRING)) {
                    tableId = loot.getString("loot_table");
                }
                if (loot.contains("seed", Tag.TAG_LONG)) {
                    seed = Long.toString(loot.getLong("seed"));
                }
            }
        }

        tableIdEntry = new LootTableSelectionEntryModel(this, tableId, v -> {});
        seedEntry = new StringEntryModel(this, ModTexts.SEED, seed, v -> {});
        getEntries().add(tableIdEntry);
        getEntries().add(seedEntry);
    }

    @Override
    public int getEntryListStart() { return -1; }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        String idRaw = java.util.Optional.ofNullable(tableIdEntry.getValue()).orElse("").trim();
        String seedRaw = java.util.Optional.ofNullable(seedEntry.getValue()).orElse("").trim();

        if (idRaw.isEmpty()) {
            CompoundTag data = getData();
            if (data != null && data.contains("components", Tag.TAG_COMPOUND)) {
                CompoundTag components = data.getCompound("components");
                components.remove("minecraft:container_loot");
                components.remove("!minecraft:container_loot");
                components.put("!minecraft:container", new CompoundTag());
                if (components.isEmpty()) data.remove("components");
            }
            stack.remove(DataComponents.CONTAINER_LOOT);
            tableIdEntry.setValid(true);
            seedEntry.setValid(true);
            return;
        }

        try {
            ResourceLocation id = ResourceLocation.parse(idRaw);
            CompoundTag loot = new CompoundTag();
            loot.putString("loot_table", id.toString());
            long seedValue = 0L;
            boolean hasSeed = false;
            if (!seedRaw.isEmpty()) {
                seedValue = Long.parseLong(seedRaw);
                hasSeed = true;
            }
            CompoundTag data = getData();
            if (data != null) {
                if (!data.contains("components", Tag.TAG_COMPOUND)) {
                    data.put("components", new CompoundTag());
                }
                CompoundTag components = data.getCompound("components");
                if (hasSeed) {
                    loot.putLong("seed", seedValue);
                }
                components.put("minecraft:container_loot", loot);
                components.remove("minecraft:container");
                components.put("!minecraft:container", new CompoundTag());
                if (components.isEmpty()) data.remove("components");
            }
            ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key =
                    ResourceKey.create(Registries.LOOT_TABLE, id);
            SeededContainerLoot seeded = new SeededContainerLoot(key, hasSeed ? seedValue : 0L);
            stack.set(DataComponents.CONTAINER_LOOT, seeded);
            stack.remove(DataComponents.CONTAINER);
            tableIdEntry.setValid(true);
            seedEntry.setValid(true);
        } catch (Exception ex) {
            tableIdEntry.setValid(false);
            seedEntry.setValid(false);
        }
    }
}
