package com.github.rinorsi.cadeditor.client.screen.model.category.block;

import com.github.rinorsi.cadeditor.client.screen.model.BlockEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.LootTableSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

/**
 * 方块端：容器战利品表 + 种子（LootTable / LootTableSeed）。
 */
public class BlockContainerLootCategoryModel extends BlockEditorCategoryModel {
    private LootTableSelectionEntryModel tableEntry;
    private StringEntryModel seedEntry;

    public BlockContainerLootCategoryModel(BlockEditorModel parent) {
        super(ModTexts.CONTAINER_LOOT, parent);
    }

    @Override
    protected void setupEntries() {
        CompoundTag tag = getData();
        String table = "";
        String seed = "";
        if (tag != null) {
            if (tag.contains("LootTable")) {
                table = tag.getString("LootTable").orElse("");
            }
            if (tag.contains("LootTableSeed")) {
                seed = tag.getLong("LootTableSeed").map(value -> Long.toString(value)).orElse("");
            }
        }
        tableEntry = new LootTableSelectionEntryModel(this, table, v -> {});
        seedEntry = new StringEntryModel(this, ModTexts.SEED, seed, v -> {});
        getEntries().add(tableEntry);
        getEntries().add(seedEntry);
    }

    @Override
    public void apply() {
        super.apply();
        CompoundTag tag = getData();
        if (tag == null) return;
        String table = Optional.ofNullable(tableEntry.getValue()).orElse("").trim();
        String seedRaw = Optional.ofNullable(seedEntry.getValue()).orElse("").trim();

        if (table.isEmpty()) {
            tag.remove("LootTable");
            tag.remove("LootTableSeed");
            return;
        }

        tag.putString("LootTable", table);
        if (seedRaw.isEmpty()) {
            tag.remove("LootTableSeed");
        } else {
            try {
                long s = Long.parseLong(seedRaw);
                tag.putLong("LootTableSeed", s);
            } catch (Exception ex) {
                seedEntry.setValid(false);
                return;
            }
        }
        if (tag.contains("Items")) {
            tag.remove("Items");
        }
    }
}
