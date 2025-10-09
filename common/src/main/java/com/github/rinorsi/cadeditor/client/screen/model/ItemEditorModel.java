package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.*;
import com.github.rinorsi.cadeditor.client.util.CompatFood;
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
import net.minecraft.world.food.FoodProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.ShieldItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ItemEditorModel extends StandardEditorModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String KEY_COMPONENTS = "components";
    private static final String KEY_LEGACY_TAG = "tag";
    private static final String TOMBSTONE_PREFIX = "!";
    private static final String FOOD_COMPONENT_KEY = "minecraft:food";

    private static final Set<String> DELETE_IF_ABSENT_KEYS = Set.of(
        "minecraft:hide_tooltip",
        "minecraft:hide_additional_tooltip",
        "minecraft:tooltip_display",
        "minecraft:enchantment_glint_override"
    );

    private static final int MIN_NUTRITION = 0;
    private static final int MAX_DURATION_TICKS = 20 * 60 * 60;
    private static final int MAX_AMPLIFIER = 255;
    private static final float MIN_EAT_SECONDS = 0.05f;

    private final FoodComponentState foodState = new FoodComponentState();
    private boolean desiredFoodEnabled;

    private ItemGeneralCategoryModel generalCategory;
    private ItemFoodPropertiesCategoryModel foodPropertiesCategory;
    private ItemFoodEffectsCategoryModel foodEffectsCategory;

    public ItemEditorModel(ItemEditorContext context) { super(context); }

    @Override
    public ItemEditorContext getContext() { return (ItemEditorContext) super.getContext(); }

    @Override
    protected void setupCategories() {
        generalCategory = new ItemGeneralCategoryModel(this);
        getCategories().add(generalCategory);
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
        if (stack.has(DataComponents.TOOL) || item instanceof DiggerItem) {
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
        if (item instanceof CompassItem || stack.has(DataComponents.LODESTONE_TRACKER)) {
            getCategories().add(new ItemLodestoneCategoryModel(this));
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
        if (stack.has(DataComponents.TRIM) || item instanceof ArmorItem) {
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
        if (stack.has(DataComponents.OMINOUS_BOTTLE_AMPLIFIER) || item == Items.OMINOUS_BOTTLE) {
            getCategories().add(new ItemOminousBottleCategoryModel(this));
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
        //TODO Note Block 音效选择/过滤器还有自定义Sound Event试听
        //TODO 烟火之星/烟花的配置面板还空着，顺便加个多阶段扩展
        //TODO 束口袋和容器网格要融合，拖拽/模板如果可以也做一下
        //TODO 可疑的炖菜的效果列表得有预设，还有概率
        //TODO 玩家头颅要加离线皮肤和历史UUID管理
        //TODO 陶罐纹饰挑选界面要补四面预览，再加扩展的纹理预设
        //TODO 方块实体数据、通用自定义数据和SNBT结构校验模板都得在这里挂入口
        //TODO 书与笔/成品书编辑器要准备富文本、撤销和语法高亮
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
        var rebuilt = context.getItemStack().save(registryAccess, new net.minecraft.nbt.CompoundTag());
        if (rebuilt instanceof net.minecraft.nbt.CompoundTag compound) {
            if (stagedLegacy != null && !stagedLegacy.isEmpty()) compound.put(KEY_LEGACY_TAG, stagedLegacy);
            else compound.remove(KEY_LEGACY_TAG);
            context.setTag(compound);
            ItemStack parsed = ItemStack.parseOptional(registryAccess, compound);
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
        if (desiredFoodEnabled) {
            writeFoodComponent(stack);
        } else {
            stack.remove(DataComponents.FOOD);
            foodState.updateOriginalUsingConvertsTo(Optional.empty());
            DebugLog.infoKey("cadeditor.debug.food.removed", describeStackForLogs(stack));
        }
        syncContextSnapshot(stack);
    }

    private void writeFoodComponent(ItemStack stack) {
        try {
            FoodProperties properties = buildSafeFoodProperties();
            stack.set(DataComponents.FOOD, properties);
            DebugLog.infoKey("cadeditor.debug.food.applied", describeStackForLogs(stack), properties.nutrition(), properties.saturation(), properties.canAlwaysEat(), properties.eatSeconds(), properties.effects());
        } catch (Throwable t) {
            LOGGER.error("Building FoodProperties failed, fallback to no-effects. Cause: {}", t.toString());
            FoodProperties safe = new FoodProperties(
                    Math.max(MIN_NUTRITION, foodState.getNutrition()),
                    Math.max(0f, foodState.getSaturation()),
                    foodState.isAlwaysEat(),
                    Math.max(MIN_EAT_SECONDS, foodState.getEatSeconds()),
                    foodState.resolveUsingConvertsTo(),
                    List.of()
            );
            stack.set(DataComponents.FOOD, safe);
        }
    }

    private FoodProperties buildSafeFoodProperties() {
        int nutrition = Math.max(MIN_NUTRITION, foodState.getNutrition());
        if (nutrition != foodState.getNutrition()) foodState.setNutrition(nutrition);
        float saturation = Math.max(0f, foodState.getSaturation());
        float eatSeconds = Math.max(MIN_EAT_SECONDS, foodState.getEatSeconds());

        var convertsTo = foodState.resolveUsingConvertsTo();
        List<FoodProperties.PossibleEffect> effects = sanitizeEffects(foodState.copyEffectsForComponent());

        FoodProperties props = new FoodProperties(
                nutrition,
                saturation,
                foodState.isAlwaysEat(),
                eatSeconds,
                convertsTo,
                effects
        );
        foodState.updateOriginalUsingConvertsTo(convertsTo);
        return props;
    }

    private List<FoodProperties.PossibleEffect> sanitizeEffects(List<FoodProperties.PossibleEffect> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        List<FoodProperties.PossibleEffect> out = new ArrayList<>(raw.size());
        for (FoodProperties.PossibleEffect pe : raw) {
            try {
                if (pe == null) continue;

                float p = pe.probability();
                if (!(p > 0f)) continue;
                if (p > 1f) p = 1f;

                MobEffectInstance inst = pe.effect();
                if (inst == null) continue;

                Holder<MobEffect> effHolder = inst.getEffect();
                if (effHolder == null) continue;

                int dur = Math.max(1, Math.min(inst.getDuration(), MAX_DURATION_TICKS));
                int amp = Math.max(0, Math.min(inst.getAmplifier(), MAX_AMPLIFIER));
                boolean ambient = inst.isAmbient();
                boolean showParticles = inst.isVisible();
                boolean showIcon = inst.showIcon();

                MobEffectInstance rebuilt =
                        new MobEffectInstance(effHolder, dur, amp, ambient, showParticles, showIcon);

                CompatFood.makePossibleEffect(rebuilt, p).ifPresent(out::add);
            } catch (Throwable t) {
                LOGGER.warn("Skip invalid food effect {} due to {}", String.valueOf(pe), t.toString());
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
        var saved = stack.save(registryAccess, new CompoundTag());
        if (!(saved instanceof CompoundTag compound)) return;

        if (!desiredFoodEnabled) {
            ensureComponentsTag(compound).put(TOMBSTONE_PREFIX + FOOD_COMPONENT_KEY, new CompoundTag());
        }

        CompoundTag oldRoot = context.getTag();
        CompoundTag legacy = copyLegacyPayload(oldRoot);
        if (legacy != null && !legacy.isEmpty()) compound.put(KEY_LEGACY_TAG, legacy);
        else compound.remove(KEY_LEGACY_TAG);

        Set<String> doNotCopyBack = desiredFoodEnabled ? Collections.emptySet() : Set.of(FOOD_COMPONENT_KEY);
        compound = mergeComponentsPreservingUnknown(oldRoot, compound, doNotCopyBack);

        if (desiredFoodEnabled && !hasComponent(compound, FOOD_COMPONENT_KEY)) {
            ensureComponentsTag(compound).put(FOOD_COMPONENT_KEY, buildMinimalFoodNbt());
        }

        context.setTag(compound);
        ItemStack parsed = ItemStack.parseOptional(registryAccess, compound);
        if (!parsed.isEmpty()) {
            if (desiredFoodEnabled && parsed.get(DataComponents.FOOD) == null) {
                writeFoodComponent(parsed);
            } else if (!desiredFoodEnabled && parsed.get(DataComponents.FOOD) != null) {
                parsed.remove(DataComponents.FOOD);
            }
            context.setItemStack(parsed);
            foodState.loadFrom(parsed.copy());
            foodState.setEnabled(desiredFoodEnabled);
        } else {
            ItemStack fallback = stack.copy();
            if (desiredFoodEnabled && fallback.get(DataComponents.FOOD) == null) writeFoodComponent(fallback);
            else if (!desiredFoodEnabled && fallback.get(DataComponents.FOOD) != null) fallback.remove(DataComponents.FOOD);
            context.setItemStack(fallback);
            foodState.loadFrom(fallback);
            foodState.setEnabled(desiredFoodEnabled);
        }
    }

    private CompoundTag buildMinimalFoodNbt() {
        CompoundTag food = new CompoundTag();
        int nutrition = Math.max(MIN_NUTRITION, foodState.getNutrition());
        if (nutrition != foodState.getNutrition()) foodState.setNutrition(nutrition);
        food.putInt("nutrition", nutrition);
        food.putFloat("saturation", Math.max(0f, foodState.getSaturation()));
        if (foodState.isAlwaysEat()) food.putBoolean("can_always_eat", true);
        float eatSeconds = Math.max(MIN_EAT_SECONDS, foodState.getEatSeconds());
        if (eatSeconds != 1.6f) food.putFloat("eat_seconds", eatSeconds);
        return food;
    }

    private static CompoundTag mergeComponentsPreservingUnknown(
            CompoundTag oldRoot, CompoundTag newRoot, Set<String> keysNotToCopy) {
    CompoundTag merged = newRoot.copy();

    if (!merged.contains(KEY_COMPONENTS, Tag.TAG_COMPOUND)) {
        if (oldRoot != null && oldRoot.contains(KEY_COMPONENTS, Tag.TAG_COMPOUND)) {
            merged.put(KEY_COMPONENTS, oldRoot.getCompound(KEY_COMPONENTS).copy());
        }
        return merged;
    }

    CompoundTag newComps = merged.getCompound(KEY_COMPONENTS);

    if (oldRoot != null && oldRoot.contains(KEY_COMPONENTS, Tag.TAG_COMPOUND)) {
        CompoundTag oldComps = oldRoot.getCompound(KEY_COMPONENTS);

        for (String oldKey : oldComps.getAllKeys()) {
            if (oldKey.startsWith(TOMBSTONE_PREFIX)) continue;
            if (keysNotToCopy != null && keysNotToCopy.contains(oldKey)) continue;

            boolean explicitlyRemoved = newComps.contains(TOMBSTONE_PREFIX + oldKey);
            boolean explicitlySet     = newComps.contains(oldKey);
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
        var saved = stack.save(getRegistryAccess(), new CompoundTag());
        if (!(saved instanceof CompoundTag compound)) return "<empty>";
        Set<String> doNotCopyBack = desiredFoodEnabled ? Collections.emptySet() : Set.of(FOOD_COMPONENT_KEY);
        CompoundTag merged = mergeComponentsPreservingUnknown(getContext().getTag(), compound.copy(), doNotCopyBack);
        return merged.toString();
    }

    private static CompoundTag copyLegacyPayload(CompoundTag container) {
        if (container == null || !container.contains(KEY_LEGACY_TAG, Tag.TAG_COMPOUND)) return null;
        CompoundTag legacy = container.getCompound(KEY_LEGACY_TAG);
        if (legacy.isEmpty()) return null;
        return legacy.copy();
    }

    private static boolean hasComponent(CompoundTag root, String key) {
        return root != null
                && root.contains(KEY_COMPONENTS, Tag.TAG_COMPOUND)
                && root.getCompound(KEY_COMPONENTS).contains(key);
    }

    private static CompoundTag ensureComponentsTag(CompoundTag root) {
        if (!root.contains(KEY_COMPONENTS, Tag.TAG_COMPOUND)) {
            root.put(KEY_COMPONENTS, new CompoundTag());
        }
        return root.getCompound(KEY_COMPONENTS);
    }
}

