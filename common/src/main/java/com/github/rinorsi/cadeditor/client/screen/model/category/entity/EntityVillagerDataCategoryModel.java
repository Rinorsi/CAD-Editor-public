package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerProfessionSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTypeSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;

public class EntityVillagerDataCategoryModel extends EntityCategoryModel {
    private static final String VILLAGER_DATA_TAG = "VillagerData";
    private static final String OFFERS_TAG = "Offers";
    private static final String RECIPES_TAG = "Recipes";
    private static final String ASSIGN_PROFESSION_WHEN_SPAWNED_TAG = "AssignProfessionWhenSpawned";
    private static final String XP_TAG = "Xp";
    private static final String NONE_PROFESSION = "minecraft:none";
    private static final int MIN_LOCKED_PROFESSION_XP = 1;
    private static final int MIN_STABLE_PROFESSION_LEVEL = 2;

    private String professionId = "minecraft:none";
    private String initialProfessionId = "minecraft:none";
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
        initialProfessionId = professionId;
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
        String profession = normalizeId(professionId, NONE_PROFESSION);
        boolean professionChanged = !profession.equals(normalizeId(initialProfessionId, NONE_PROFESSION));
        villagerData.putString("profession", profession);
        villagerData.putString("type", normalizeId(typeId, "minecraft:plains"));
        int villagerLevel = clampLevel(level);
        if (professionChanged
                && !NONE_PROFESSION.equals(profession)
                && villagerLevel < MIN_STABLE_PROFESSION_LEVEL) {
            villagerLevel = MIN_STABLE_PROFESSION_LEVEL;
        }
        villagerData.putInt("level", villagerLevel);
        data.put(VILLAGER_DATA_TAG, villagerData);

        if (!NONE_PROFESSION.equals(profession)) {
            data.putBoolean(ASSIGN_PROFESSION_WHEN_SPAWNED_TAG, false);
            if (!hasCustomOffers(data) && data.getIntOr(XP_TAG, 0) < MIN_LOCKED_PROFESSION_XP) {
                data.putInt(XP_TAG, MIN_LOCKED_PROFESSION_XP);
            }
        }
    }

    private static boolean hasCustomOffers(CompoundTag root) {
        CompoundTag offers = root.getCompound(OFFERS_TAG).orElse(null);
        if (offers == null) {
            return false;
        }
        return offers.getList(RECIPES_TAG).map(list -> !list.isEmpty()).orElse(false);
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

