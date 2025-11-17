package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerProfessionSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTypeSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;

public class EntityVillagerDataCategoryModel extends EntityCategoryModel {
    private static final String VILLAGER_DATA_TAG = "VillagerData";

    private String professionId = "minecraft:none";
    private String typeId = "minecraft:plains";
    private int level = 1;

    public EntityVillagerDataCategoryModel(EntityEditorModel editor) {
        super(ModTexts.VILLAGER_DATA, editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = getData();
        CompoundTag villagerData = data.getCompound(VILLAGER_DATA_TAG).orElseGet(CompoundTag::new);

        professionId = normalizeId(villagerData.getString("profession").orElse(null), "minecraft:none");
        typeId = normalizeId(villagerData.getString("type").orElse(null), "minecraft:plains");
        level = villagerData.getIntOr("level", 1);

        getEntries().add(new VillagerProfessionSelectionEntryModel(this, ModTexts.VILLAGER_PROFESSION, professionId,
                value -> professionId = normalizeId(value, "minecraft:none")));
        getEntries().add(new VillagerTypeSelectionEntryModel(this, ModTexts.VILLAGER_TYPE, typeId,
                value -> typeId = normalizeId(value, "minecraft:plains")));
        getEntries().add(new IntegerEntryModel(this, ModTexts.VILLAGER_LEVEL, clampLevel(level),
                value -> level = clampLevel(value), value -> value >= 1 && value <= 5));
    }

    @Override
    public void apply() {
        super.apply();
        writeVillagerData();
    }

    private void writeVillagerData() {
        CompoundTag data = getData();
        CompoundTag villagerData = data.getCompound(VILLAGER_DATA_TAG).orElseGet(CompoundTag::new);
        villagerData.putString("profession", normalizeId(professionId, "minecraft:none"));
        villagerData.putString("type", normalizeId(typeId, "minecraft:plains"));
        villagerData.putInt("level", clampLevel(level));
        data.put(VILLAGER_DATA_TAG, villagerData);
    }

    private static String normalizeId(String value, String defaultValue) {
        String result = value == null || value.isBlank() ? defaultValue : value;
        if (!result.contains(":")) {
            result = "minecraft:" + result;
        }
        return result;
    }

    private static int clampLevel(int level) {
        if (level < 1) {
            return 1;
        }
        if (level > 5) {
            return 5;
        }
        return level;
    }
}

