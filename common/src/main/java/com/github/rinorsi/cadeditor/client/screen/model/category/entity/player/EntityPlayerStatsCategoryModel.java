package com.github.rinorsi.cadeditor.client.screen.model.category.entity.player;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.mixin.FoodDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.storage.TagValueInput;

/**
 * Handles player specific stats such as experience, hunger and absorption.
 */
public class EntityPlayerStatsCategoryModel extends EntityCategoryModel {
    private static final String XP_LEVEL_TAG = "XpLevel";
    private static final String XP_TOTAL_TAG = "XpTotal";
    private static final String XP_PROGRESS_TAG = "XpP";
    private static final String FOOD_LEVEL_TAG = "foodLevel";
    private static final String FOOD_SATURATION_TAG = "foodSaturationLevel";
    private static final String FOOD_EXHAUSTION_TAG = "foodExhaustionLevel";
    private static final String ABSORPTION_TAG = "AbsorptionAmount";
    private static final String SLEEPING_TAG = "Sleeping";

    private int xpLevel;
    private int xpTotal;
    private float xpProgress;
    private int foodLevel;
    private float foodSaturation;
    private float foodExhaustion;
    private float absorption;
    private boolean sleeping;

    public EntityPlayerStatsCategoryModel(EntityEditorModel editor) {
        super(Component.translatable("cadeditor.gui.player_stats"), editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = ensurePlayerTag();
        xpLevel = NbtHelper.getInt(data, XP_LEVEL_TAG, 0);
        xpTotal = NbtHelper.getInt(data, XP_TOTAL_TAG, 0);
        xpProgress = NbtHelper.getFloat(data, XP_PROGRESS_TAG, 0f);
        foodLevel = NbtHelper.getInt(data, FOOD_LEVEL_TAG, 20);
        foodSaturation = NbtHelper.getFloat(data, FOOD_SATURATION_TAG, 5f);
        foodExhaustion = NbtHelper.getFloat(data, FOOD_EXHAUSTION_TAG, 0f);
        absorption = NbtHelper.getFloat(data, ABSORPTION_TAG, 0f);
        sleeping = NbtHelper.getBoolean(data, SLEEPING_TAG, false);

        getEntries().add(new IntegerEntryModel(this, Component.translatable("cadeditor.gui.xp_level"), xpLevel, value -> xpLevel = Math.max(0, value)));
        getEntries().add(new IntegerEntryModel(this, Component.translatable("cadeditor.gui.xp_total"), xpTotal, value -> xpTotal = Math.max(0, value)));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.xp_progress"), xpProgress, value -> xpProgress = clamp(value, 0f, 1f)));
        getEntries().add(new IntegerEntryModel(this, Component.translatable("cadeditor.gui.food_level"), foodLevel, value -> foodLevel = clampInt(value, 0, 20)));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.food_saturation"), foodSaturation, value -> foodSaturation = Math.max(0f, value)));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.food_exhaustion"), foodExhaustion, value -> foodExhaustion = Math.max(0f, value)));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.absorption"), absorption, value -> absorption = Math.max(0f, value)));
        getEntries().add(new BooleanEntryModel(this, Component.translatable("cadeditor.gui.sleeping"), sleeping, value -> sleeping = value));
    }

    @Override
    public void apply() {
        super.apply();
        CompoundTag data = ensurePlayerTag();
        data.putInt(XP_LEVEL_TAG, Math.max(0, xpLevel));
        data.putInt(XP_TOTAL_TAG, Math.max(0, xpTotal));
        data.putFloat(XP_PROGRESS_TAG, clamp(xpProgress, 0f, 1f));
        data.putInt(FOOD_LEVEL_TAG, clampInt(foodLevel, 0, 20));
        data.putFloat(FOOD_SATURATION_TAG, Math.max(0f, foodSaturation));
        data.putFloat(FOOD_EXHAUSTION_TAG, Math.max(0f, foodExhaustion));
        if (absorption > 0f) {
            data.putFloat(ABSORPTION_TAG, absorption);
        } else {
            data.remove(ABSORPTION_TAG);
        }
        data.putBoolean(SLEEPING_TAG, sleeping);
        syncPlayerInstance();
    }

    private void syncPlayerInstance() {
        Player player = getEntity() instanceof Player p ? p : null;
        if (player == null) {
            return;
        }
        player.experienceLevel = Math.max(0, xpLevel);
        player.totalExperience = Math.max(0, xpTotal);
        player.experienceProgress = clamp(xpProgress, 0f, 1f);
        FoodData foodData = player.getFoodData();
        int clampedFood = clampInt(foodLevel, 0, 20);
        float clampedSaturation = Math.max(0f, foodSaturation);
        float clampedExhaustion = Math.max(0f, foodExhaustion);
        foodData.setFoodLevel(clampedFood);
        foodData.setSaturation(clampedSaturation);
        if (foodData instanceof FoodDataAccessor accessor) {
            accessor.cadeditor$setExhaustionLevel(clampedExhaustion);
        } else {
            CompoundTag foodSyncTag = new CompoundTag();
            foodSyncTag.putInt(FOOD_LEVEL_TAG, clampedFood);
            foodSyncTag.putFloat(FOOD_SATURATION_TAG, clampedSaturation);
            foodSyncTag.putFloat(FOOD_EXHAUSTION_TAG, clampedExhaustion);
            var registries = ClientUtil.registryAccess();
            if (registries != null) {
                foodData.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, registries, foodSyncTag));
            }
        }
        player.setAbsorptionAmount(Math.max(0f, absorption));
        if (sleeping) {
            player.setSleepingPos(player.blockPosition());
        } else {
            player.stopSleeping();
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private CompoundTag ensurePlayerTag() {
        CompoundTag data = getData();
        if (data == null) {
            data = new CompoundTag();
            getContext().setTag(data);
        }
        return data;
    }
}
