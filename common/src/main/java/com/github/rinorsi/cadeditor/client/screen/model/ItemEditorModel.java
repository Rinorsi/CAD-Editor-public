package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.*;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.entity.EquipmentSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
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
        "minecraft:tooltip_display",
        "minecraft:enchantment_glint_override",
        "minecraft:custom_name"
    );
    private static final String TOOLTIP_DISPLAY_COMPONENT_KEY = "minecraft:tooltip_display";
    private static final String HIDE_TOOLTIP_COMPONENT_KEY = "minecraft:hide_tooltip";
    private static final String POTION_CONTENTS_COMPONENT_KEY = "minecraft:potion_contents";
    private static final String WRITABLE_BOOK_COMPONENT_KEY = "minecraft:writable_book_content";
    private static final String SHOW_IN_TOOLTIP_FIELD = "show_in_tooltip";
    private static final String LEVELS_FIELD = "levels";
    private static final Set<String> COMPONENTS_WITH_TOOLTIP_BOOLEAN = Set.of(
            "minecraft:enchantments",
            "minecraft:stored_enchantments",
            "minecraft:attribute_modifiers",
            "minecraft:unbreakable",
            "minecraft:can_break",
            "minecraft:can_place_on",
            "minecraft:dyed_color",
            "minecraft:trim",
            "minecraft:jukebox_playable"
    );
    private static final Set<String> TOMBSTONE_COMPONENT_IDS = Set.of(
            "minecraft:lore",
            "minecraft:attribute_modifiers",
            "minecraft:rarity",
            "minecraft:repair_cost",
            "minecraft:enchantments",
            "minecraft:stored_enchantments"
    );

    private static final int MIN_NUTRITION = 0;
    private static final int MAX_DURATION_TICKS = 20 * 60 * 60;
    private static final int MAX_AMPLIFIER = 255;

    private final FoodComponentState foodState = new FoodComponentState();
    private final EnumMap<ItemExtraToggle, Boolean> extraComponentStates = new EnumMap<>(ItemExtraToggle.class);
    private boolean desiredFoodEnabled;
    private boolean desiredConsumableEnabled;
    private boolean shouldSyncExtraComponentsFromStack = true;

    private ItemGeneralCategoryModel generalCategory;
    private ItemExtraComponentsCategoryModel extraComponentsCategory;

    public ItemEditorModel(ItemEditorContext context) {
        super(context);
        resetExtraComponentStates();
    }

    @Override
    public ItemEditorContext getContext() { return (ItemEditorContext) super.getContext(); }

    @Override
    public void initalize() {
        super.initalize();
        getCategories().forEach(category -> category.initalize());
    }

    @Override
    protected void setupCategories() {
        ItemStack stack = getContext().getItemStack();
        Item item = stack.getItem();

        if (shouldSyncExtraComponentsFromStack) {
            syncExtraComponentStatesFromStack(stack);
            shouldSyncExtraComponentsFromStack = false;
        }

        desiredFoodEnabled = isExtraComponentEnabled(ItemExtraToggle.FOOD);
        desiredConsumableEnabled = isExtraComponentEnabled(ItemExtraToggle.CONSUMABLE);
        foodState.loadFrom(stack);
        foodState.setEnabled(desiredFoodEnabled);
        foodState.setConsumableStandaloneEnabled(desiredConsumableEnabled);

        extraComponentsCategory = new ItemExtraComponentsCategoryModel(this);
        getCategories().add(extraComponentsCategory);

        generalCategory = new ItemGeneralCategoryModel(this);
        getCategories().add(generalCategory);

        getCategories().add(new ItemDisplayCategoryModel(this));
        if (isExtraComponentEnabled(ItemExtraToggle.CUSTOM_MODEL_DATA)) {
            getCategories().add(new ItemCustomModelDataCategoryModel(this));
        }
        if (isExtraComponentEnabled(ItemExtraToggle.EQUIPPABLE)) {
            getCategories().add(new ItemEquippableCategoryModel(this));
        }
        boolean showToolCategory = isExtraComponentEnabled(ItemExtraToggle.TOOL)
                && (stack.has(DataComponents.TOOL) || stack.is(ItemTags.MINING_ENCHANTABLE));
        if (showToolCategory) {
            getCategories().add(new ItemToolCategoryModel(this));
        }
        if (isExtraComponentEnabled(ItemExtraToggle.ENCHANTMENTS)) {
            getCategories().add(new ItemEnchantmentsCategoryModel(this));
        }

        getCategories().add(new ItemEquipmentTraitsCategoryModel(this));

        if (desiredFoodEnabled) {
            getCategories().add(new ItemFoodPropertiesCategoryModel(this));
            getCategories().add(new ItemFoodEffectsCategoryModel(this));
        }

        getCategories().add(new ItemHideFlagsCategoryModel(this));

        if (isExtraComponentEnabled(ItemExtraToggle.ATTRIBUTE_MODIFIERS)) {
            getCategories().add(new ItemAttributeModifiersCategoryModel(this));
        }

        if (isExtraComponentEnabled(ItemExtraToggle.DEATH_PROTECTION)) {
            getCategories().add(new ItemDeathProtectionCategoryModel(this));
        }

        if (desiredFoodEnabled || desiredConsumableEnabled) {
            getCategories().add(new ItemConsumableCategoryModel(this));
        }

        if (item instanceof BlockItem) {
            getCategories().add(new ItemBlockListCategoryModel(ModTexts.CAN_PLACE_ON, this, "CanPlaceOn"));
        }
        getCategories().add(new ItemBlockListCategoryModel(ModTexts.CAN_DESTROY, this, "CanDestroy"));

        if (stack.is(ItemTags.DYEABLE) || stack.has(DataComponents.DYED_COLOR)) {
            getCategories().add(new ItemDyeableCategoryModel(this));
        }
        if (item instanceof SpawnEggItem spawnEgg) {
            getCategories().add(new ItemSpawnEggCategoryModel(this, spawnEgg));
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
                    block instanceof net.minecraft.world.level.block.ShulkerBoxBlock
                            || block instanceof net.minecraft.world.level.block.ChestBlock
                            || block instanceof net.minecraft.world.level.block.BarrelBlock
                            || block instanceof net.minecraft.world.level.block.DispenserBlock
                            || block instanceof net.minecraft.world.level.block.DropperBlock
                            || block instanceof net.minecraft.world.level.block.HopperBlock;
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
        if (item instanceof PotionItem || item instanceof TippedArrowItem) {
            getCategories().add(new ItemPotionEffectsCategoryModel(this));
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

    public boolean isExtraComponentEnabled(ItemExtraToggle toggle) {
        return extraComponentStates.getOrDefault(toggle, defaultExtraComponentState(toggle));
    }

    public void setExtraComponentEnabled(ItemExtraToggle toggle, boolean enabled) {
        boolean current = isExtraComponentEnabled(toggle);
        if (current == enabled) return;
        extraComponentStates.put(toggle, enabled);
        if (toggle == ItemExtraToggle.FOOD) {
            if (enabled && !desiredFoodEnabled) {
                foodState.prepareForInitialEnable(getContext().getItemStack());
                desiredFoodEnabled = true;
            } else if (!enabled && desiredFoodEnabled) {
                desiredFoodEnabled = false;
            } else {
                desiredFoodEnabled = enabled;
            }
            applyFoodComponent();
        } else if (toggle == ItemExtraToggle.DEATH_PROTECTION) {
            ItemStack stack = getContext().getItemStack();
            if (enabled) {
                if (stack.get(DataComponents.DEATH_PROTECTION) == null) {
                    stack.set(DataComponents.DEATH_PROTECTION, DeathProtection.TOTEM_OF_UNDYING);
                }
            } else {
                stack.remove(DataComponents.DEATH_PROTECTION);
                removeComponentFromDataTag("minecraft:death_protection");
            }
        } else if (toggle == ItemExtraToggle.EQUIPPABLE && !enabled) {
            ItemStack stack = getContext().getItemStack();
            stack.remove(DataComponents.EQUIPPABLE);
            removeComponentFromDataTag("minecraft:equippable");
        } else if (toggle == ItemExtraToggle.CONSUMABLE) {
            desiredConsumableEnabled = enabled;
            foodState.setConsumableStandaloneEnabled(desiredConsumableEnabled);
            applyFoodComponent();
        }
        rebuildCategoriesPreservingSelection(null);
    }

    public void enableFoodComponent() {
        setExtraComponentEnabled(ItemExtraToggle.FOOD, true);
    }

    public void disableFoodComponent() {
        setExtraComponentEnabled(ItemExtraToggle.FOOD, false);
    }

    private void resetExtraComponentStates() {
        extraComponentStates.clear();
        for (ItemExtraToggle toggle : ItemExtraToggle.values()) {
            extraComponentStates.put(toggle, defaultExtraComponentState(toggle));
        }
    }

    private boolean defaultExtraComponentState(ItemExtraToggle toggle) {
        return switch (toggle) {
            case ATTRIBUTE_MODIFIERS, TOOL, ENCHANTMENTS -> true;
            default -> false;
        };
    }

    private void syncExtraComponentStatesFromStack(ItemStack stack) {
        if (stack.has(DataComponents.EQUIPPABLE)) {
            extraComponentStates.put(ItemExtraToggle.EQUIPPABLE, true);
        }
        if (stack.has(DataComponents.CUSTOM_MODEL_DATA)) {
            extraComponentStates.put(ItemExtraToggle.CUSTOM_MODEL_DATA, true);
        }
        if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
            extraComponentStates.put(ItemExtraToggle.ATTRIBUTE_MODIFIERS, true);
        }
        if (stack.has(DataComponents.ENCHANTMENTS) || stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            extraComponentStates.put(ItemExtraToggle.ENCHANTMENTS, true);
        }
        if (stack.has(DataComponents.TOOL) || stack.is(ItemTags.MINING_ENCHANTABLE)) {
            extraComponentStates.put(ItemExtraToggle.TOOL, true);
        }
        if (stack.has(DataComponents.DEATH_PROTECTION)) {
            extraComponentStates.put(ItemExtraToggle.DEATH_PROTECTION, true);
        }
        if (stack.has(DataComponents.FOOD)) {
            extraComponentStates.put(ItemExtraToggle.FOOD, true);
        }
        if (stack.has(DataComponents.CONSUMABLE) || stack.has(DataComponents.USE_REMAINDER)) {
            extraComponentStates.put(ItemExtraToggle.CONSUMABLE, true);
        }
    }

    public void applyFoodComponent() {
        ItemStack stack = getContext().getItemStack();
        foodState.setEnabled(desiredFoodEnabled);
        foodState.setConsumableStandaloneEnabled(desiredConsumableEnabled);
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
        resetExtraComponentStates();
        shouldSyncExtraComponentsFromStack = true;
        rebuildCategoriesPreservingSelection(null);
    }

    private void rebuildCategoriesPreservingSelection(Class<?> preferredCategoryClass) {
        CategoryModel previousSelection = getSelectedCategory();
        Class<?> targetClass = preferredCategoryClass != null
                ? preferredCategoryClass
                : (previousSelection == null ? null : previousSelection.getClass());

        getCategories().clear();
        setupCategories();
        getCategories().forEach(CategoryModel::initalize);

        if (!getCategories().isEmpty()) {
            CategoryModel toSelect = getCategories().get(0);
            if (targetClass != null) {
                for (CategoryModel category : getCategories()) {
                    if (category.getClass() == targetClass) {
                        toSelect = category;
                        break;
                    }
                }
            }
            setSelectedCategory(toSelect);
        }
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

    static CompoundTag mergeComponentsPreservingUnknown(
            CompoundTag oldRoot, CompoundTag newRoot, Set<String> keysNotToCopy) {
        CompoundTag merged = newRoot.copy();

        if (!merged.contains(KEY_COMPONENTS)) {
            if (oldRoot != null) {
                oldRoot.getCompound(KEY_COMPONENTS).ifPresent(oldComps -> merged.put(KEY_COMPONENTS, oldComps.copy()));
            }
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

        applyComponentMigrations(merged);
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

    private static void applyComponentMigrations(CompoundTag root) {
        migrateLegacyEnchantments(root);
        migrateLegacyHideFlags(root);
        migrateLegacyAttributeModifiers(root);
        migrateLegacyAdventurePredicates(root);
        migrateLegacyEntityTag(root);
        migrateLegacyPotionContents(root);
        migrateLegacyWritableBookPages(root);
    }

    private static void migrateLegacyEnchantments(CompoundTag root) {
        CompoundTag legacyTag = root.getCompound(KEY_LEGACY_TAG).orElse(null);
        if (legacyTag == null) {
            return;
        }
        migrateLegacyEnchantmentList(legacyTag, root, "Enchantments", "minecraft:enchantments");
        migrateLegacyEnchantmentList(legacyTag, root, "StoredEnchantments", "minecraft:stored_enchantments");
        if (legacyTag.isEmpty()) {
            root.remove(KEY_LEGACY_TAG);
        }
    }

    private static void migrateLegacyEnchantmentList(CompoundTag legacyTag, CompoundTag root, String legacyKey, String componentKey) {
        ListTag enchantList = legacyTag.getList(legacyKey).orElse(null);
        if (enchantList == null || enchantList.isEmpty()) {
            legacyTag.remove(legacyKey);
            return;
        }
        CompoundTag components = ensureComponentsTag(root);
        if (components.contains(componentKey)) {
            legacyTag.remove(legacyKey);
            return;
        }
        CompoundTag levels = new CompoundTag();
        boolean hasData = false;
        for (Tag element : enchantList) {
            if (!(element instanceof CompoundTag enchantment)) {
                continue;
            }
            String id = enchantment.getString("id").orElse("");
            if (id.isEmpty()) {
                continue;
            }
            if (!id.contains(":")) {
                id = "minecraft:" + id;
            }
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) {
                continue;
            }
            int level = enchantment.getIntOr("lvl", 0);
            if (level <= 0) {
                continue;
            }
            levels.putInt(rl.toString(), level);
            hasData = true;
        }
        if (hasData) {
            CompoundTag component = new CompoundTag();
            component.put(LEVELS_FIELD, levels);
            components.put(componentKey, component);
        }
        legacyTag.remove(legacyKey);
    }

    private static void migrateLegacyHideFlags(CompoundTag root) {
        CompoundTag components = root.getCompound(KEY_COMPONENTS).orElse(null);
        CompoundTag legacyTag = root.getCompound(KEY_LEGACY_TAG).orElse(null);
        boolean hideTooltip = false;
        EnumSet<ItemHideFlagsCategoryModel.HideFlag> hiddenFlags = EnumSet.noneOf(ItemHideFlagsCategoryModel.HideFlag.class);

        if (components != null) {
            if (components.contains(HIDE_TOOLTIP_COMPONENT_KEY)) {
                hideTooltip = true;
                components.remove(HIDE_TOOLTIP_COMPONENT_KEY);
            }
            List<String> keys = List.copyOf(components.keySet());
            for (String key : keys) {
                if (!key.startsWith(TOMBSTONE_PREFIX)) {
                    continue;
                }
                String target = key.substring(1);
                if (TOOLTIP_DISPLAY_COMPONENT_KEY.equals(target)
                        || HIDE_TOOLTIP_COMPONENT_KEY.equals(target)
                        || TOMBSTONE_COMPONENT_IDS.contains(target)) {
                    components.remove(key);
                }
            }
        }

        if (legacyTag != null && legacyTag.contains("HideFlags")) {
            int mask = legacyTag.getIntOr("HideFlags", 0);
            if (mask != 0) {
                for (ItemHideFlagsCategoryModel.HideFlag flag : ItemHideFlagsCategoryModel.HideFlag.values()) {
                    if (flag == ItemHideFlagsCategoryModel.HideFlag.OTHER) {
                        if ((mask & flag.getValue()) != 0) {
                            hideTooltip = true;
                        }
                        continue;
                    }
                    if ((mask & flag.getValue()) != 0) {
                        hiddenFlags.add(flag);
                    }
                }
            }
            legacyTag.remove("HideFlags");
        }

        if (!hideTooltip && hiddenFlags.isEmpty()) {
            if (legacyTag != null && legacyTag.isEmpty()) {
                root.remove(KEY_LEGACY_TAG);
            }
            return;
        }

        CompoundTag comps = ensureComponentsTag(root);
        Set<String> hiddenComponentIds = new LinkedHashSet<>();
        for (ItemHideFlagsCategoryModel.HideFlag flag : hiddenFlags) {
            for (DataComponentType<?> type : flag.hiddenComponents()) {
                String id = componentId(type);
                if (id == null) {
                    continue;
                }
                hiddenComponentIds.add(id);
                if (COMPONENTS_WITH_TOOLTIP_BOOLEAN.contains(id)) {
                    setComponentTooltipVisibility(comps, id, false);
                }
            }
        }

        if (!hiddenComponentIds.isEmpty() || hideTooltip || !comps.contains(TOOLTIP_DISPLAY_COMPONENT_KEY)) {
            writeTooltipDisplayTag(comps, hideTooltip, hiddenComponentIds);
        }

        if (legacyTag != null && legacyTag.isEmpty()) {
            root.remove(KEY_LEGACY_TAG);
        }
    }

    private static void migrateLegacyAttributeModifiers(CompoundTag root) {
        CompoundTag legacyTag = root.getCompound(KEY_LEGACY_TAG).orElse(null);
        if (legacyTag == null) {
            return;
        }
        ListTag legacyList = legacyTag.getList("AttributeModifiers").orElse(null);
        if (legacyList == null) {
            return;
        }
        CompoundTag components = ensureComponentsTag(root);
        if (!components.contains("minecraft:attribute_modifiers")) {
            ListTag modifiers = new ListTag();
            Set<UUID> usedUuids = new HashSet<>();
            Set<ResourceLocation> usedModifierIds = new HashSet<>();
            for (Tag element : legacyList) {
                if (!(element instanceof CompoundTag legacyModifier)) {
                    continue;
                }
                String attributeId = normalizeNamespacedId(legacyModifier.getStringOr("AttributeName", ""));
                ResourceLocation attributeKey = ResourceLocation.tryParse(attributeId);
                if (attributeKey == null) {
                    continue;
                }
                int operation = legacyModifier.getIntOr("Operation", 0);
                String operationName = switch (operation) {
                    case 1 -> "add_multiplied_base";
                    case 2 -> "add_multiplied_total";
                    default -> "add_value";
                };
                double amount = legacyModifier.getDoubleOr("Amount", 0d);
                String slot = legacyModifier.getStringOr("Slot", "").trim();
                UUID uuid = readLegacyModifierUuid(legacyModifier);
                if (uuid == null || !usedUuids.add(uuid)) {
                    uuid = generateModifierUuid(legacyModifier, usedUuids);
                }
                ResourceLocation modifierId = createModifierId(uuid, usedModifierIds);

                CompoundTag modifier = new CompoundTag();
                modifier.putString("type", attributeKey.toString());
                modifier.putDouble("amount", amount);
                modifier.putString("operation", operationName);
                modifier.putString("id", modifierId.toString());
                if (!slot.isEmpty()) {
                    modifier.putString("slot", slot);
                }
                modifiers.add(modifier);
            }
            if (!modifiers.isEmpty()) {
                CompoundTag component = new CompoundTag();
                component.put("modifiers", modifiers);
                components.put("minecraft:attribute_modifiers", component);
            }
        }
        legacyTag.remove("AttributeModifiers");
        if (legacyTag.isEmpty()) {
            root.remove(KEY_LEGACY_TAG);
        }
    }

    private static void migrateLegacyAdventurePredicates(CompoundTag root) {
        CompoundTag legacyTag = root.getCompound(KEY_LEGACY_TAG).orElse(null);
        if (legacyTag == null) {
            return;
        }
        migrateLegacyAdventurePredicateList(legacyTag, root, "CanDestroy", "minecraft:can_break");
        migrateLegacyAdventurePredicateList(legacyTag, root, "CanPlaceOn", "minecraft:can_place_on");
        if (legacyTag.isEmpty()) {
            root.remove(KEY_LEGACY_TAG);
        }
    }

    private static void migrateLegacyAdventurePredicateList(CompoundTag legacyTag, CompoundTag root, String legacyKey, String componentKey) {
        ListTag legacyList = legacyTag.getList(legacyKey).orElse(null);
        if (legacyList == null || legacyList.isEmpty()) {
            legacyTag.remove(legacyKey);
            return;
        }
        CompoundTag components = ensureComponentsTag(root);
        if (components.contains(componentKey)) {
            legacyTag.remove(legacyKey);
            return;
        }
        ListTag predicates = new ListTag();
        for (Tag element : legacyList) {
            if (!(element instanceof StringTag selectorTag)) {
                continue;
            }
            String selector = normalizeBlockSelector(selectorTag.value());
            if (selector == null || selector.isEmpty()) {
                continue;
            }
            CompoundTag predicate = new CompoundTag();
            predicate.putString("blocks", selector);
            predicates.add(predicate);
        }
        if (!predicates.isEmpty()) {
            CompoundTag component = new CompoundTag();
            component.put("predicates", predicates);
            components.put(componentKey, component);
        }
        legacyTag.remove(legacyKey);
    }

    private static void migrateLegacyEntityTag(CompoundTag root) {
        CompoundTag legacyTag = root.getCompound(KEY_LEGACY_TAG).orElse(null);
        if (legacyTag == null) {
            return;
        }
        CompoundTag entityTag = legacyTag.getCompound("EntityTag").orElse(null);
        if (entityTag != null) {
            CompoundTag components = ensureComponentsTag(root);
            if (!components.contains("minecraft:entity_data")) {
                components.put("minecraft:entity_data", entityTag.copy());
            }
            legacyTag.remove("EntityTag");
        }
        if (legacyTag.isEmpty()) {
            root.remove(KEY_LEGACY_TAG);
        }
    }

    private static void migrateLegacyPotionContents(CompoundTag root) {
        CompoundTag legacyTag = root.getCompound(KEY_LEGACY_TAG).orElse(null);
        if (legacyTag == null) {
            return;
        }
        boolean hasLegacyPotion = legacyTag.contains("Potion")
                || legacyTag.contains("CustomPotionColor")
                || legacyTag.contains("custom_potion_effects");
        if (!hasLegacyPotion) {
            return;
        }

        CompoundTag components = ensureComponentsTag(root);
        if (!components.contains(POTION_CONTENTS_COMPONENT_KEY)) {
            CompoundTag potionContents = new CompoundTag();
            String potionId = normalizeNamespacedId(legacyTag.getStringOr("Potion", "").trim());
            if (!potionId.isEmpty()) {
                potionContents.putString("potion", potionId);
            }
            legacyTag.getInt("CustomPotionColor").ifPresent(color -> {
                if (color != 0) {
                    potionContents.putInt("custom_color", color);
                }
            });

            ListTag customEffects = legacyTag.getList("custom_potion_effects").orElse(null);
            if (customEffects != null && !customEffects.isEmpty()) {
                ListTag migratedEffects = new ListTag();
                for (Tag element : customEffects) {
                    if (!(element instanceof CompoundTag effect)) {
                        continue;
                    }
                    String id = normalizeNamespacedId(effect.getStringOr("id", ""));
                    if (id.isEmpty()) {
                        id = normalizeNamespacedId(effect.getStringOr("Id", ""));
                    }
                    if (id.isEmpty()) {
                        continue;
                    }
                    CompoundTag migrated = new CompoundTag();
                    migrated.putString("id", id);
                    migrated.putInt("amplifier", Math.max(0, effect.getIntOr("amplifier", effect.getIntOr("Amplifier", 0))));
                    migrated.putInt("duration", Math.max(1, effect.getIntOr("duration", effect.getIntOr("Duration", 1))));
                    migrated.putBoolean("ambient", effect.getBooleanOr("ambient", effect.getBooleanOr("Ambient", false)));
                    boolean showParticles = effect.getBoolean("show_particles")
                            .or(() -> effect.getBoolean("ShowParticles"))
                            .orElse(true);
                    boolean showIcon = effect.getBoolean("show_icon")
                            .or(() -> effect.getBoolean("ShowIcon"))
                            .orElse(true);
                    migrated.putBoolean("show_particles", showParticles);
                    migrated.putBoolean("show_icon", showIcon);
                    migratedEffects.add(migrated);
                }
                if (!migratedEffects.isEmpty()) {
                    potionContents.put("custom_effects", migratedEffects);
                }
            }

            if (!potionContents.isEmpty()) {
                components.put(POTION_CONTENTS_COMPONENT_KEY, potionContents);
            }
        }

        legacyTag.remove("Potion");
        legacyTag.remove("CustomPotionColor");
        legacyTag.remove("custom_potion_effects");
        if (legacyTag.isEmpty()) {
            root.remove(KEY_LEGACY_TAG);
        }
    }

    private static void migrateLegacyWritableBookPages(CompoundTag root) {
        CompoundTag legacyTag = root.getCompound(KEY_LEGACY_TAG).orElse(null);
        if (legacyTag == null || !legacyTag.contains("pages")) {
            return;
        }
        ListTag pages = legacyTag.getList("pages").orElse(null);
        if (pages == null) {
            legacyTag.remove("pages");
            if (legacyTag.isEmpty()) {
                root.remove(KEY_LEGACY_TAG);
            }
            return;
        }

        CompoundTag components = ensureComponentsTag(root);
        if (!components.contains(WRITABLE_BOOK_COMPONENT_KEY)) {
            ListTag migratedPages = new ListTag();
            for (int i = 0; i < pages.size() && migratedPages.size() < WritableBookContent.MAX_PAGES; i++) {
                String page = pages.getString(i).orElse("");
                if (page.contains("\r")) {
                    page = page.replace("\r", "");
                }
                if (page.length() > WritableBookContent.PAGE_EDIT_LENGTH) {
                    page = page.substring(0, WritableBookContent.PAGE_EDIT_LENGTH);
                }
                migratedPages.add(StringTag.valueOf(page));
            }
            if (!migratedPages.isEmpty()) {
                CompoundTag writableBook = new CompoundTag();
                writableBook.put("pages", migratedPages);
                components.put(WRITABLE_BOOK_COMPONENT_KEY, writableBook);
            }
        }

        legacyTag.remove("pages");
        if (legacyTag.isEmpty()) {
            root.remove(KEY_LEGACY_TAG);
        }
    }

    private static String normalizeBlockSelector(String raw) {
        if (raw == null) {
            return null;
        }
        String selector = raw.trim();
        if (selector.isEmpty()) {
            return null;
        }
        if (selector.startsWith("#")) {
            String value = selector.substring(1);
            ResourceLocation rl = value.contains(":")
                    ? ResourceLocation.tryParse(value)
                    : ResourceLocation.tryParse("minecraft:" + value);
            return rl == null ? null : "#" + rl;
        }
        String value = selector.contains(":") ? selector : "minecraft:" + selector;
        ResourceLocation rl = ResourceLocation.tryParse(value);
        return rl == null ? null : rl.toString();
    }

    private static String normalizeNamespacedId(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String candidate = raw.trim();
        if (!candidate.contains(":")) {
            candidate = "minecraft:" + candidate;
        }
        ResourceLocation rl = ResourceLocation.tryParse(candidate);
        return rl == null ? "" : rl.toString();
    }

    private static UUID readLegacyModifierUuid(CompoundTag tag) {
        UUID parsed = parseUuidString(tag.getStringOr("UUID", ""));
        if (parsed != null) {
            return parsed;
        }
        parsed = parseUuidString(tag.getStringOr("id", ""));
        if (parsed != null) {
            return parsed;
        }
        parsed = parseUuidString(tag.getStringOr("uuid", ""));
        if (parsed != null) {
            return parsed;
        }
        parsed = tag.getIntArray("UUID").map(ItemEditorModel::uuidFromIntArray).orElse(null);
        if (parsed != null) {
            return parsed;
        }
        parsed = tag.getIntArray("id").map(ItemEditorModel::uuidFromIntArray).orElse(null);
        if (parsed != null) {
            return parsed;
        }
        return tag.getIntArray("uuid").map(ItemEditorModel::uuidFromIntArray).orElse(null);
    }

    private static UUID generateModifierUuid(CompoundTag legacyModifier, Set<UUID> usedIds) {
        CompoundTag seedTag = legacyModifier.copy();
        seedTag.remove("UUID");
        seedTag.remove("uuid");
        seedTag.remove("id");
        String seed = seedTag.toString();
        int salt = 0;
        while (true) {
            UUID generated = UUID.nameUUIDFromBytes((seed + "#" + salt).getBytes(StandardCharsets.UTF_8));
            if (usedIds.add(generated)) {
                return generated;
            }
            salt++;
        }
    }

    private static ResourceLocation createModifierId(UUID uuid, Set<ResourceLocation> usedIds) {
        String compact = uuid.toString().replace("-", "");
        String basePath = "m_" + compact.substring(0, 12);
        ResourceLocation direct = ResourceLocation.fromNamespaceAndPath("cadeditor", basePath);
        if (usedIds.add(direct)) {
            return direct;
        }
        int suffix = 1;
        while (true) {
            ResourceLocation withSuffix = ResourceLocation.fromNamespaceAndPath(
                    "cadeditor",
                    basePath + "_" + Integer.toHexString(suffix++)
            );
            if (usedIds.add(withSuffix)) {
                return withSuffix;
            }
        }
    }

    private static UUID parseUuidString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return UUID.fromString(trimmed);
        } catch (IllegalArgumentException ignored) {
        }
        String candidate = trimmed;
        int underscore = candidate.lastIndexOf('_');
        if (underscore >= 0 && underscore + 1 < candidate.length()) {
            candidate = candidate.substring(underscore + 1);
        } else if (candidate.contains(":")) {
            candidate = candidate.substring(candidate.lastIndexOf(':') + 1);
        }
        candidate = candidate.replace("-", "");
        if (candidate.length() != 32) {
            return null;
        }
        try {
            long most = Long.parseUnsignedLong(candidate.substring(0, 16), 16);
            long least = Long.parseUnsignedLong(candidate.substring(16), 16);
            return new UUID(most, least);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static UUID uuidFromIntArray(int[] data) {
        if (data == null || data.length != 4) {
            return null;
        }
        long most = ((long) data[0] << 32) | (data[1] & 0xffffffffL);
        long least = ((long) data[2] << 32) | (data[3] & 0xffffffffL);
        return new UUID(most, least);
    }

    private static void writeTooltipDisplayTag(CompoundTag components, boolean hideTooltip, Set<String> hiddenComponentIds) {
        if (!hideTooltip && hiddenComponentIds.isEmpty()) {
            components.remove(TOOLTIP_DISPLAY_COMPONENT_KEY);
            return;
        }
        CompoundTag tooltip = components.getCompound(TOOLTIP_DISPLAY_COMPONENT_KEY).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            components.put(TOOLTIP_DISPLAY_COMPONENT_KEY, created);
            return created;
        });
        if (hideTooltip) {
            tooltip.putBoolean("hide_tooltip", true);
        } else {
            tooltip.remove("hide_tooltip");
        }
        if (!hiddenComponentIds.isEmpty()) {
            ListTag list = new ListTag();
            for (String id : hiddenComponentIds) {
                list.add(StringTag.valueOf(id));
            }
            tooltip.put("hidden_components", list);
        } else {
            tooltip.remove("hidden_components");
        }
        if (tooltip.isEmpty()) {
            components.remove(TOOLTIP_DISPLAY_COMPONENT_KEY);
        }
    }

    private static void setComponentTooltipVisibility(CompoundTag components, String componentId, boolean show) {
        components.getCompound(componentId).ifPresent(comp -> {
            if (show) {
                comp.remove(SHOW_IN_TOOLTIP_FIELD);
            } else {
                comp.putBoolean(SHOW_IN_TOOLTIP_FIELD, false);
            }
        });
    }

    private static String componentId(DataComponentType<?> type) {
        if (type == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
        return id == null ? null : id.toString();
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
        return desiredFoodEnabled || desiredConsumableEnabled;
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
        if (!isExtraComponentEnabled(ItemExtraToggle.DEATH_PROTECTION)) {
            suppressed.add("minecraft:death_protection");
        }
        return suppressed;
    }

    public void removeComponentFromDataTag(String componentId) {
        var context = getContext();
        CompoundTag data = context.getTag();
        if (data == null) {
            return;
        }
        data.getCompound(KEY_COMPONENTS).ifPresent(components -> {
            components.remove(componentId);
            if (components.isEmpty()) {
                data.remove(KEY_COMPONENTS);
            }
        });
        context.setTag(data);
    }
}
