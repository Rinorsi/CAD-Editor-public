package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.*;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.entity.EquipmentSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ItemEditorModel extends StandardEditorModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String KEY_COMPONENTS = "components";
    private static final String KEY_LEGACY_TAG = "tag";
    private static final String TOMBSTONE_PREFIX = "!";
    private static final String FOOD_COMPONENT_KEY = "minecraft:food";
    private static final String CONSUMABLE_COMPONENT_KEY = "minecraft:consumable";
    private static final String USE_REMAINDER_COMPONENT_KEY = "minecraft:use_remainder";
    private static final Set<String> FOOD_COMPONENT_KEYS = Set.of(
        FOOD_COMPONENT_KEY,
        CONSUMABLE_COMPONENT_KEY,
        USE_REMAINDER_COMPONENT_KEY
    );

    private static final Set<String> DELETE_IF_ABSENT_KEYS = Set.of(
        "minecraft:hide_tooltip",
        "minecraft:hide_additional_tooltip",
        "minecraft:tooltip_display",
        "minecraft:enchantment_glint_override",
        "minecraft:custom_name"
    );

    private static final int MIN_NUTRITION = 0;
    private static final int MAX_DURATION_TICKS = 20 * 60 * 60;
    private static final int MAX_AMPLIFIER = 255;

    private final FoodComponentState foodState = new FoodComponentState();
    private boolean desiredFoodEnabled;

    private ItemGeneralCategoryModel generalCategory;
    private ItemFoodPropertiesCategoryModel foodPropertiesCategory;
    private ItemFoodEffectsCategoryModel foodEffectsCategory;

    public ItemEditorModel(ItemEditorContext context) { super(context); }

    @Override
    public ItemEditorContext getContext() { return (ItemEditorContext) super.getContext(); }

    @Override
    public void initalize() {
        super.initalize();
        getCategories().forEach(category -> category.initalize());
    }

    @Override
    protected void setupCategories() {
        generalCategory = new ItemGeneralCategoryModel(this);
        getCategories().add(generalCategory);
        getCategories().add(new ItemCustomModelDataCategoryModel(this));
        getCategories().add(new ItemConsumableCategoryModel(this));
        getCategories().add(new ItemDisplayCategoryModel(this));
        getCategories().add(new ItemEnchantmentsCategoryModel(this));
        ItemStack stack = getContext().getItemStack();
        Item item = stack.getItem();
        getCategories().add(new ItemAttributeModifiersCategoryModel(this));
        getCategories().add(new ItemHideFlagsCategoryModel(this));
        if (stack.is(ItemTags.DYEABLE) || stack.has(DataComponents.DYED_COLOR)) {
            getCategories().add(new ItemDyeableCategoryModel(this));
        }
        if (item instanceof SpawnEggItem spawnEgg) {
            getCategories().add(new ItemSpawnEggCategoryModel(this, spawnEgg));
        }
        if (stack.has(DataComponents.TOOL) || stack.is(ItemTags.MINING_ENCHANTABLE)) {
            getCategories().add(new ItemToolCategoryModel(this));
        }
        if (item instanceof MapItem || stack.has(DataComponents.MAP_ID) || stack.has(DataComponents.MAP_COLOR)
                || stack.has(DataComponents.MAP_DECORATIONS) || stack.has(DataComponents.MAP_POST_PROCESSING)) {
            getCategories().add(new ItemMapCategoryModel(this));
            getCategories().add(new ItemMapDecorationsCategoryModel(this));
        }
        if (item instanceof CrossbowItem || stack.has(DataComponents.CHARGED_PROJECTILES)) {
            getCategories().add(new ItemCrossbowCategoryModel(this));
        }
        if (stack.has(DataComponents.BUNDLE_CONTENTS) || item == Items.BUNDLE) {
            getCategories().add(new ItemBundleContentsCategoryModel(this));
        }
        if (item instanceof CompassItem || stack.has(DataComponents.LODESTONE_TRACKER)) {
            getCategories().add(new ItemLodestoneCategoryModel(this));
        }
        if (item == Items.SUSPICIOUS_STEW) {
            getCategories().add(new ItemSuspiciousStewEffectsCategoryModel(this));
        }
        if (item == Items.PLAYER_HEAD) {
            getCategories().add(new ItemProfileCategoryModel(this));
        }
        boolean isContainerBlockItem = false;
        if (item instanceof BlockItem bi) {
            var block = bi.getBlock();
            isContainerBlockItem =
                block instanceof net.minecraft.world.level.block.ShulkerBoxBlock ||
                block instanceof net.minecraft.world.level.block.ChestBlock ||
                block instanceof net.minecraft.world.level.block.BarrelBlock ||
                block instanceof net.minecraft.world.level.block.DispenserBlock ||
                block instanceof net.minecraft.world.level.block.DropperBlock ||
                block instanceof net.minecraft.world.level.block.HopperBlock;
        }
        if (stack.has(DataComponents.CONTAINER) || isContainerBlockItem) {
            getCategories().add(new ItemContainerGridCategoryModel(this));
            if (stack.has(DataComponents.CONTAINER_LOOT) || isContainerBlockItem) {
                getCategories().add(new ItemContainerLootCategoryModel(this));
            }
        }
        if (stack.has(DataComponents.TRIM) || isArmorStack(stack)) {
            getCategories().add(new ItemTrimCategoryModel(this));
        }
        if (stack.has(DataComponents.BANNER_PATTERNS) || stack.has(DataComponents.BASE_COLOR)
                || item instanceof BannerItem || item instanceof ShieldItem) {
            getCategories().add(new ItemBannerPatternCategoryModel(this));
        }
        if (stack.has(DataComponents.BUCKET_ENTITY_DATA) || item instanceof MobBucketItem) {
            getCategories().add(new ItemBucketEntityCategoryModel(this));
        }
        if (stack.has(DataComponents.INSTRUMENT) || item == Items.GOAT_HORN) {
            getCategories().add(new ItemInstrumentCategoryModel(this));
        }
        if (stack.has(DataComponents.POT_DECORATIONS) || item == Items.DECORATED_POT) {
            getCategories().add(new ItemPotDecorationsCategoryModel(this));
        }
        // Temporarily disable the note block sound editor until the backend supports it reliably.
        // if (stack.has(DataComponents.NOTE_BLOCK_SOUND) || item == Items.NOTE_BLOCK) {
        //     getCategories().add(new ItemNoteBlockCategoryModel(this));
        // }
        if (stack.has(DataComponents.FIREWORK_EXPLOSION) || item == Items.FIREWORK_STAR) {
            getCategories().add(new ItemFireworkStarCategoryModel(this));
        }
        if (stack.has(DataComponents.FIREWORKS) || item == Items.FIREWORK_ROCKET) {
            getCategories().add(new ItemFireworksCategoryModel(this));
        }
        if (stack.has(DataComponents.OMINOUS_BOTTLE_AMPLIFIER) || item == Items.OMINOUS_BOTTLE) {
            getCategories().add(new ItemOminousBottleCategoryModel(this));
        }
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            getCategories().add(new ItemCustomDataCategoryModel(this));
        }
        if (item == Items.WRITABLE_BOOK) {
            getCategories().add(new ItemWritableBookPagesCategoryModel(this));
        }
        getCategories().add(new ItemBlockListCategoryModel(ModTexts.CAN_DESTROY, this, "CanDestroy"));
        foodState.loadFrom(stack);
        desiredFoodEnabled = foodState.isEnabled();

        if (item instanceof PotionItem || item instanceof TippedArrowItem) {
            getCategories().add(new ItemPotionEffectsCategoryModel(this));
        }
        if (desiredFoodEnabled) attachFoodCategories();
        if (item instanceof BlockItem) {
            getCategories().add(new ItemBlockListCategoryModel(ModTexts.CAN_PLACE_ON, this, "CanPlaceOn"));
        }
    }
        //TODO 方块状态、调试棒、直接Damage写入这些比较小众的功能可以考虑放到一个“实验性”分类里
        //TODO 创造栏锁定的行为还要验证，等弄清楚再开放
        //TODO 新分类的脚手架要更加模板化，顺手把数据导入导出也连起来
        //TODO 全局搜索和跨页高亮也要回到这里统一调度
    @Override
    public void apply() {
        var context = getContext();
        super.apply();
        applyFoodComponent();

        CompoundTag stagedLegacy = copyLegacyPayload(context.getTag());
        var registryAccess = getRegistryAccess();
        var rebuilt = ClientUtil.saveItemStack(registryAccess, context.getItemStack());
        if (rebuilt instanceof net.minecraft.nbt.CompoundTag compound) {
            if (stagedLegacy != null && !stagedLegacy.isEmpty()) compound.put(KEY_LEGACY_TAG, stagedLegacy);
            else compound.remove(KEY_LEGACY_TAG);
            context.setTag(compound);
            ItemStack parsed = ClientUtil.parseItemStack(registryAccess, compound);
            context.setItemStack(parsed.isEmpty() ? context.getItemStack().copy() : parsed);
        }
    }

    public FoodComponentState getFoodState() { return foodState; }

    public void enableFoodComponent() {
        if (desiredFoodEnabled) return;
        foodState.prepareForInitialEnable(getContext().getItemStack());
        desiredFoodEnabled = true;
        applyFoodComponent();
        attachFoodCategories();
        if (generalCategory != null) generalCategory.syncFoodToggle();
    }

    public void disableFoodComponent() {
        if (!desiredFoodEnabled) return;
        desiredFoodEnabled = false;
        applyFoodComponent();
        detachFoodCategories();
        if (generalCategory != null) generalCategory.syncFoodToggle();
    }

    public void applyFoodComponent() {
        ItemStack stack = getContext().getItemStack();
        foodState.setEnabled(desiredFoodEnabled);
        boolean wantsFood = desiredFoodEnabled;
        boolean wantsConsumable = wantsConsumableComponent();
        if (wantsFood) {
            writeFoodComponents(stack);
        } else if (wantsConsumable) {
            writeStandaloneConsumable(stack);
        } else {
            stack.remove(DataComponents.FOOD);
            stack.remove(DataComponents.CONSUMABLE);
            stack.remove(DataComponents.USE_REMAINDER);
            foodState.updateOriginalUsingConvertsTo(Optional.empty());
            DebugLog.infoKey("cadeditor.debug.food.removed", describeStackForLogs(stack));
        }
        syncContextSnapshot(stack);
    }

    private void writeStandaloneConsumable(ItemStack stack) {
        try {
            List<ConsumeEffect> statusEffects = buildStatusEffects();
            Consumable consumable = foodState.buildConsumable(statusEffects);
            Optional<ItemStack> convertsTo = foodState.resolveUsingConvertsTo();
            Optional<UseRemainder> useRemainder = foodState.buildUseRemainder(convertsTo);

            stack.remove(DataComponents.FOOD);
            stack.set(DataComponents.CONSUMABLE, consumable);
            if (useRemainder.isPresent()) {
                stack.set(DataComponents.USE_REMAINDER, useRemainder.get());
            } else {
                stack.remove(DataComponents.USE_REMAINDER);
            }

            foodState.updateOriginalUsingConvertsTo(convertsTo);
            DebugLog.infoKey(
                    "cadeditor.debug.food.applied_consumable",
                    describeStackForLogs(stack),
                    consumable.consumeSeconds(),
                    statusEffects
            );
        } catch (Throwable t) {
            LOGGER.error("Building consumable component failed, falling back to minimal values. Cause: {}", t.toString());
            stack.remove(DataComponents.FOOD);
            Consumable.Builder builder = Consumable.builder()
                    .consumeSeconds(foodState.getConsumeSeconds())
                    .hasConsumeParticles(foodState.hasConsumeParticles())
                    .animation(foodState.getAnimation());
            foodState.getConsumeSound().ifPresent(builder::sound);
            stack.set(DataComponents.CONSUMABLE, builder.build());
            stack.remove(DataComponents.USE_REMAINDER);
            foodState.updateOriginalUsingConvertsTo(Optional.empty());
        }
    }

    private void writeFoodComponents(ItemStack stack) {
        try {
            FoodProperties properties = buildSafeFoodProperties();
            List<ConsumeEffect> statusEffects = buildStatusEffects();
            Consumable consumable = foodState.buildConsumable(statusEffects);
            Optional<ItemStack> convertsTo = foodState.resolveUsingConvertsTo();
            Optional<UseRemainder> useRemainder = foodState.buildUseRemainder(convertsTo);

            stack.set(DataComponents.FOOD, properties);
            stack.set(DataComponents.CONSUMABLE, consumable);
            if (useRemainder.isPresent()) {
                stack.set(DataComponents.USE_REMAINDER, useRemainder.get());
            } else {
                stack.remove(DataComponents.USE_REMAINDER);
            }

            foodState.updateOriginalUsingConvertsTo(convertsTo);
            DebugLog.infoKey(
                    "cadeditor.debug.food.applied",
                    describeStackForLogs(stack),
                    properties.nutrition(),
                    properties.saturation(),
                    properties.canAlwaysEat(),
                    consumable.consumeSeconds(),
                    statusEffects
            );
        } catch (Throwable t) {
            LOGGER.error("Building food components failed, falling back to minimal values. Cause: {}", t.toString());
            FoodProperties safeFood = new FoodProperties(
                    Math.max(MIN_NUTRITION, foodState.getNutrition()),
                    Math.max(0f, foodState.getSaturation()),
                    foodState.isAlwaysEat()
            );
            stack.set(DataComponents.FOOD, safeFood);
            Consumable.Builder builder = Consumable.builder()
                    .consumeSeconds(foodState.getConsumeSeconds())
                    .hasConsumeParticles(foodState.hasConsumeParticles())
                    .animation(foodState.getAnimation());
            foodState.getConsumeSound().ifPresent(builder::sound);
            Consumable fallbackConsumable = builder.build();
            stack.set(DataComponents.CONSUMABLE, fallbackConsumable);
            stack.remove(DataComponents.USE_REMAINDER);
            foodState.updateOriginalUsingConvertsTo(Optional.empty());
        }
    }

    private FoodProperties buildSafeFoodProperties() {
        int nutrition = Math.max(MIN_NUTRITION, foodState.getNutrition());
        if (nutrition != foodState.getNutrition()) foodState.setNutrition(nutrition);
        float saturation = Math.max(0f, foodState.getSaturation());
        if (saturation != foodState.getSaturation()) foodState.setSaturation(saturation);

        return new FoodProperties(
                nutrition,
                saturation,
                foodState.isAlwaysEat()
        );
    }

    private List<ConsumeEffect> buildStatusEffects() {
        List<FoodComponentState.FoodEffectData> raw = foodState.copyEffectsForComponent();
        if (raw == null || raw.isEmpty()) return List.of();
        List<ConsumeEffect> out = new ArrayList<>(raw.size());
        for (FoodComponentState.FoodEffectData data : raw) {
            try {
                if (data == null) continue;

                float probability = data.probability();
                if (!(probability > 0f)) continue;
                if (probability > 1f) probability = 1f;

                MobEffectInstance inst = data.effect();
                if (inst == null) continue;

                Holder<MobEffect> effectHolder = inst.getEffect();
                if (effectHolder == null) continue;

                int dur = Math.max(1, Math.min(inst.getDuration(), MAX_DURATION_TICKS));
                int amp = Math.max(0, Math.min(inst.getAmplifier(), MAX_AMPLIFIER));
                boolean ambient = inst.isAmbient();
                boolean showParticles = inst.isVisible();
                boolean showIcon = inst.showIcon();

                MobEffectInstance rebuilt = new MobEffectInstance(
                        effectHolder,
                        dur,
                        amp,
                        ambient,
                        showParticles,
                        showIcon
                );

                out.add(new ApplyStatusEffectsConsumeEffect(rebuilt, probability));
            } catch (Throwable t) {
                LOGGER.warn("Skip invalid food effect {} due to {}", String.valueOf(data), t.toString());
            }
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    public void handleStackReplaced(ItemStack newStack) {
        ItemStack stack = newStack == null ? ItemStack.EMPTY : newStack.copy();
        syncContextSnapshot(stack);
        desiredFoodEnabled = foodState.isEnabled();

        var previousSelection = getSelectedCategory();
        Class<?> previousCategoryClass = previousSelection == null ? null : previousSelection.getClass();

        getCategories().clear();
        setupCategories();
        getCategories().forEach(category -> category.initalize());

        if (!getCategories().isEmpty()) {
            var toSelect = getCategories().get(0);
            if (previousCategoryClass != null) {
                for (var category : getCategories()) {
                    if (category.getClass() == previousCategoryClass) {
                        toSelect = category;
                        break;
                    }
                }
            }
            setSelectedCategory(toSelect);
        }

        if (generalCategory != null) {
            generalCategory.syncFoodToggle();
        }
    }

    private void attachFoodCategories() {
        detachFoodCategories();
        foodPropertiesCategory = new ItemFoodPropertiesCategoryModel(this);
        foodEffectsCategory = new ItemFoodEffectsCategoryModel(this);
        int insertIndex = findFoodInsertIndex();
        getCategories().add(insertIndex, foodPropertiesCategory);
        getCategories().add(insertIndex + 1, foodEffectsCategory);
        foodPropertiesCategory.initalize();
        foodEffectsCategory.initalize();
    }

    private void detachFoodCategories() {
        boolean wasSelected = getSelectedCategory() == foodPropertiesCategory || getSelectedCategory() == foodEffectsCategory;
        if (foodPropertiesCategory != null) getCategories().remove(foodPropertiesCategory);
        if (foodEffectsCategory != null) getCategories().remove(foodEffectsCategory);
        if (wasSelected && !getCategories().isEmpty()) setSelectedCategory(getCategories().get(0));
        foodPropertiesCategory = null;
        foodEffectsCategory = null;
    }

    private int findFoodInsertIndex() {
        for (int i = 0; i < getCategories().size(); i++) {
            if (getCategories().get(i).getName().equals(ModTexts.CAN_PLACE_ON)) return i;
        }
        return getCategories().size();
    }

    private void syncContextSnapshot(ItemStack stack) {
        var context = getContext();
        var registryAccess = getRegistryAccess();
        var saved = ClientUtil.saveItemStack(registryAccess, stack);
        if (!(saved instanceof CompoundTag compound)) return;

        foodState.setEnabled(desiredFoodEnabled);
        boolean wantsFood = desiredFoodEnabled;
        boolean wantsConsumable = wantsConsumableComponent();

        Set<String> suppressedKeys = suppressedComponentKeys();
        if (!suppressedKeys.isEmpty()) {
            CompoundTag components = ensureComponentsTag(compound);
            for (String key : suppressedKeys) {
                components.put(TOMBSTONE_PREFIX + key, new CompoundTag());
            }
        }

        CompoundTag oldRoot = context.getTag();
        CompoundTag legacy = copyLegacyPayload(oldRoot);
        if (legacy != null && !legacy.isEmpty()) compound.put(KEY_LEGACY_TAG, legacy);
        else compound.remove(KEY_LEGACY_TAG);

        Set<String> doNotCopyBack = suppressedKeys.isEmpty() ? Collections.emptySet() : suppressedKeys;
        compound = mergeComponentsPreservingUnknown(oldRoot, compound, doNotCopyBack);

        if (wantsFood && !hasComponent(compound, FOOD_COMPONENT_KEY)) {
            ensureComponentsTag(compound).put(FOOD_COMPONENT_KEY, buildMinimalFoodNbt());
        }

        context.setTag(compound);
        ItemStack parsed = ClientUtil.parseItemStack(registryAccess, compound);
        if (!parsed.isEmpty()) {
            if (wantsFood && parsed.get(DataComponents.FOOD) == null) {
                writeFoodComponents(parsed);
            } else if (!wantsFood && parsed.get(DataComponents.FOOD) != null) {
                parsed.remove(DataComponents.FOOD);
            }

            if (wantsConsumable && parsed.get(DataComponents.CONSUMABLE) == null) {
                if (wantsFood) {
                    writeFoodComponents(parsed);
                } else {
                    writeStandaloneConsumable(parsed);
                }
            } else if (!wantsConsumable && parsed.get(DataComponents.CONSUMABLE) != null) {
                parsed.remove(DataComponents.CONSUMABLE);
                parsed.remove(DataComponents.USE_REMAINDER);
            }
            context.setItemStack(parsed);
            foodState.loadFrom(parsed.copy());
            foodState.setEnabled(desiredFoodEnabled);
        } else {
            ItemStack fallback = stack.copy();
            if (wantsFood && fallback.get(DataComponents.FOOD) == null) {
                writeFoodComponents(fallback);
            } else if (!wantsFood && fallback.get(DataComponents.FOOD) != null) {
                fallback.remove(DataComponents.FOOD);
            }

            if (wantsConsumable && fallback.get(DataComponents.CONSUMABLE) == null) {
                if (wantsFood) {
                    writeFoodComponents(fallback);
                } else {
                    writeStandaloneConsumable(fallback);
                }
            } else if (!wantsConsumable && fallback.get(DataComponents.CONSUMABLE) != null) {
                fallback.remove(DataComponents.CONSUMABLE);
                fallback.remove(DataComponents.USE_REMAINDER);
            }
            context.setItemStack(fallback);
            foodState.loadFrom(fallback);
            foodState.setEnabled(desiredFoodEnabled);
        }
    }

    private CompoundTag buildMinimalFoodNbt() {
        CompoundTag food = new CompoundTag();
        int nutrition = Math.max(MIN_NUTRITION, foodState.getNutrition());
        if (nutrition != foodState.getNutrition()) foodState.setNutrition(nutrition);
        float saturation = Math.max(0f, foodState.getSaturation());
        if (saturation != foodState.getSaturation()) foodState.setSaturation(saturation);
        food.putInt("nutrition", nutrition);
        food.putFloat("saturation", saturation);
        if (foodState.isAlwaysEat()) food.putBoolean("can_always_eat", true);
        return food;
    }

    private static CompoundTag mergeComponentsPreservingUnknown(
            CompoundTag oldRoot, CompoundTag newRoot, Set<String> keysNotToCopy) {
        CompoundTag merged = newRoot.copy();

        if (!merged.contains(KEY_COMPONENTS)) {
            if (oldRoot != null) {
                oldRoot.getCompound(KEY_COMPONENTS).ifPresent(oldComps -> merged.put(KEY_COMPONENTS, oldComps.copy()));
            }
            return merged;
        }

        CompoundTag newComps = merged.getCompound(KEY_COMPONENTS).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            merged.put(KEY_COMPONENTS, created);
            return created;
        });

        if (oldRoot != null && oldRoot.contains(KEY_COMPONENTS)) {
            CompoundTag oldComps = oldRoot.getCompound(KEY_COMPONENTS).orElse(null);

            if (oldComps != null) for (String oldKey : oldComps.keySet()) {
                if (oldKey.startsWith(TOMBSTONE_PREFIX)) continue;
                if (keysNotToCopy != null && keysNotToCopy.contains(oldKey)) continue;

                boolean explicitlyRemoved = newComps.contains(TOMBSTONE_PREFIX + oldKey);
                boolean explicitlySet = newComps.contains(oldKey);
                if (explicitlyRemoved || explicitlySet) continue;

                Tag oldVal = oldComps.get(oldKey);
                boolean isUnitLike = (oldVal instanceof CompoundTag c) && c.isEmpty();

                boolean respectDeletion = isUnitLike || DELETE_IF_ABSENT_KEYS.contains(oldKey);
                if (!respectDeletion) {
                    newComps.put(oldKey, oldVal.copy());
                }
            }
        }

        return merged;
    }

    protected HolderLookup.Provider getRegistryAccess() { return ClientUtil.registryAccess(); }

    private String describeStackForLogs(ItemStack stack) {
        if (stack.isEmpty()) return "<empty>";
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id + " tag=" + stackTagForLogs(stack);
    }

    private String stackTagForLogs(ItemStack stack) {
        if (stack.isEmpty()) return "<empty>";
        CompoundTag compound = ClientUtil.saveItemStack(getRegistryAccess(), stack);
        Set<String> doNotCopyBack = suppressedComponentKeys();
        CompoundTag merged = mergeComponentsPreservingUnknown(getContext().getTag(), compound.copy(), doNotCopyBack);
        return merged.toString();
    }

    private static CompoundTag copyLegacyPayload(CompoundTag container) {
        if (container == null) return null;
        return container.getCompound(KEY_LEGACY_TAG)
                .filter(legacy -> !legacy.isEmpty())
                .map(CompoundTag::copy)
                .orElse(null);
    }

    private static boolean hasComponent(CompoundTag root, String key) {
        return root != null
                && root.contains(KEY_COMPONENTS)
                && root.getCompound(KEY_COMPONENTS).map(comp -> comp.contains(key)).orElse(false);
    }

    private static CompoundTag ensureComponentsTag(CompoundTag root) {
        if (!root.contains(KEY_COMPONENTS)) {
            root.put(KEY_COMPONENTS, new CompoundTag());
        }
        return root.getCompound(KEY_COMPONENTS).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            root.put(KEY_COMPONENTS, created);
            return created;
        });
    }

    private static boolean isArmorStack(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().isArmor();
    }

    private boolean wantsConsumableComponent() {
        return desiredFoodEnabled || foodState.isConsumableStandaloneEnabled();
    }

    private Set<String> suppressedComponentKeys() {
        Set<String> suppressed = new HashSet<>();
        if (!desiredFoodEnabled) {
            suppressed.add(FOOD_COMPONENT_KEY);
        }
        if (!wantsConsumableComponent()) {
            suppressed.add(CONSUMABLE_COMPONENT_KEY);
            suppressed.add(USE_REMAINDER_COMPONENT_KEY);
        }
        return suppressed;
    }
}
