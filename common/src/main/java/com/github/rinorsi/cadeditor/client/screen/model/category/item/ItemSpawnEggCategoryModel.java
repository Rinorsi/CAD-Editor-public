package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.storage.TagValueInput;

import java.util.Set;

public class ItemSpawnEggCategoryModel extends ItemEditorCategoryModel {
    private static final Set<String> TRANSIENT_ENTITY_TAG_KEYS = Set.of(
            "Pos",
            "Motion",
            "Rotation",
            "UUID",
            "UUIDMost",
            "UUIDLeast"
    );
    private static final String EQUIPMENT_TAG = "equipment";
    private static final String DROP_CHANCES_TAG = "drop_chances";
    private static final String LEGACY_HAND_ITEMS_TAG = "HandItems";
    private static final String LEGACY_HAND_DROPS_TAG = "HandDropChances";
    private static final String LEGACY_ARMOR_ITEMS_TAG = "ArmorItems";
    private static final String LEGACY_ARMOR_DROPS_TAG = "ArmorDropChances";
    private static final String VILLAGER_DATA_TAG = "VillagerData";
    private static final String OFFERS_TAG = "Offers";
    private static final String RECIPES_TAG = "Recipes";
    private static final String ASSIGN_PROFESSION_WHEN_SPAWNED_TAG = "AssignProfessionWhenSpawned";
    private static final String XP_TAG = "Xp";
    private static final String NONE_PROFESSION = "minecraft:none";
    private static final String DEFAULT_TRADING_PROFESSION = "minecraft:farmer";
    private static final String DEFAULT_VILLAGER_TYPE = "minecraft:plains";
    private static final int MIN_LOCKED_PROFESSION_XP = 1;
    private static final int MIN_STABLE_PROFESSION_LEVEL = 2;
    private static final float DEFAULT_DROP_CHANCE = 0.085f;
    private static final float DROP_EPSILON = 1.0e-4f;

    private final SpawnEggItem item;
    private CompoundTag spawnData;
    private CompoundTag initialSerializedData = new CompoundTag();
    private CompoundTag initialEditorData = new CompoundTag();
    private EntityEntryModel entityEntry;

    public ItemSpawnEggCategoryModel(ItemEditorModel editor, SpawnEggItem item) {
        super(ModTexts.SPAWN_EGG, editor);
        this.item = item;
        spawnData = readSpawnData(editor.getContext().getItemStack(), editor.getContext().getTag());
    }

    @Override
    protected void setupEntries() {
        var stack = getParent().getContext().getItemStack();
        var registries = ClientUtil.registryAccess();
        CompoundTag editorData = prepareEditorData(spawnData, stack);
        var valueInput = TagValueInput.create(ProblemReporter.DISCARDING, registries, editorData);
        entityEntry = new EntityEntryModel(this,
                EntityType.by(valueInput).orElse(item.getType(stack)),
                editorData,
                value -> {
                });
        getEntries().add(entityEntry.withWeight(0));
        initialSerializedData = spawnData == null ? new CompoundTag() : spawnData.copy();
        initialEditorData = editorData.copy();
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        if (stack == null) {
            return;
        }
        CompoundTag editorValue = sanitizeEntityData(entityEntry == null ? spawnData : entityEntry.copyValue());
        CompoundTag selectedData = editorValue.equals(initialEditorData) ? initialSerializedData.copy() : editorValue.copy();
        enforceVillagerLevelFallbackOnProfessionChange(selectedData, initialSerializedData);
        CompoundTag sanitizedData = sanitizeSpawnEggComponentPayload(selectedData);
        spawnData = sanitizedData.copy();

        CompoundTag itemData = getData();
        CompoundTag legacyTag = itemData.getCompound("tag").orElse(null);
        if (legacyTag != null && legacyTag.contains("EntityTag")) {
            legacyTag.remove("EntityTag");
            if (legacyTag.isEmpty()) {
                itemData.remove("tag");
            }
        }
        if (sanitizedData.isEmpty()
                || !sanitizedData.contains("id")
                || sanitizedData.getString("id").orElse("").isEmpty()) {
            stack.remove(DataComponents.ENTITY_DATA);
            spawnData = new CompoundTag();
            initialSerializedData = new CompoundTag();
            initialEditorData = new CompoundTag();
            return;
        }
        EntityType<?> entityType = resolveEntityType(stack, sanitizedData);
        CompoundTag componentPayload = sanitizedData.copy();
        componentPayload.remove("id");
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(entityType, componentPayload));
        spawnData = componentPayload.copy();
        spawnData.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
        initialSerializedData = spawnData.copy();
        initialEditorData = editorValue.copy();
    }

    private static CompoundTag readSpawnData(ItemStack stack, CompoundTag rootTag) {
        if (stack != null) {
            TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
            if (data != null) {
                CompoundTag tag = data.copyTagWithoutId();
                tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(data.type()).toString());
                return tag;
            }
        }
        if (rootTag != null) {
            CompoundTag legacy = rootTag.getCompound("tag").orElse(null);
            if (legacy != null) {
                CompoundTag entityTag = legacy.getCompound("EntityTag").orElse(null);
                if (entityTag != null) {
                    return entityTag.copy();
                }
            }
        }
        return new CompoundTag();
    }

    private CompoundTag prepareEditorData(CompoundTag source, ItemStack stack) {
        CompoundTag normalized = sanitizeSpawnEggComponentPayload(source);
        EntityType<?> type = resolveEntityType(stack, normalized);
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            normalized.putString("id", key.toString());
        }
        return normalized;
    }

    private static CompoundTag sanitizeEntityData(CompoundTag source) {
        if (source == null || source.isEmpty()) {
            return new CompoundTag();
        }
        CompoundTag sanitized = source.copy();
        String id = sanitized.getStringOr("id", "").trim();
        if (id.isEmpty()) {
            sanitized.remove("id");
            return sanitized;
        }
        Identifier parsed = ClientUtil.parseResourceLocation(id);
        sanitized.putString("id", parsed == null ? id : parsed.toString());
        return sanitized;
    }

    static CompoundTag sanitizeSpawnEggComponentPayload(CompoundTag source) {
        CompoundTag sanitized = sanitizeEntityData(source);
        if (sanitized.isEmpty()) {
            return sanitized;
        }
        migrateLegacyEquipmentData(sanitized);
        normalizeVillagerTradeData(sanitized);
        for (String key : TRANSIENT_ENTITY_TAG_KEYS) {
            sanitized.remove(key);
        }
        float health = sanitized.getFloatOr("Health", Float.NaN);
        if (!Float.isNaN(health) && health <= 0f) {
            sanitized.remove("Health");
        }
        return sanitized;
    }

    private EntityType<?> resolveEntityType(ItemStack stack, CompoundTag data) {
        String id = data == null ? "" : data.getString("id").orElse("");
        Identifier parsed = ClientUtil.parseResourceLocation(id);
        if (parsed != null && BuiltInRegistries.ENTITY_TYPE.containsKey(parsed)) {
            return BuiltInRegistries.ENTITY_TYPE.getValue(parsed);
        }
        return item.getType(stack);
    }

    private static void migrateLegacyEquipmentData(CompoundTag tag) {
        if (tag == null) {
            return;
        }
        boolean hasLegacy = tag.contains(LEGACY_HAND_ITEMS_TAG)
                || tag.contains(LEGACY_HAND_DROPS_TAG)
                || tag.contains(LEGACY_ARMOR_ITEMS_TAG)
                || tag.contains(LEGACY_ARMOR_DROPS_TAG);
        if (!hasLegacy) {
            return;
        }

        CompoundTag equipment = tag.getCompound(EQUIPMENT_TAG).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        CompoundTag dropChances = tag.getCompound(DROP_CHANCES_TAG).map(CompoundTag::copy).orElseGet(CompoundTag::new);

        migrateLegacySlot(tag, equipment, dropChances, "mainhand", LEGACY_HAND_ITEMS_TAG, LEGACY_HAND_DROPS_TAG, 0);
        migrateLegacySlot(tag, equipment, dropChances, "offhand", LEGACY_HAND_ITEMS_TAG, LEGACY_HAND_DROPS_TAG, 1);
        migrateLegacySlot(tag, equipment, dropChances, "feet", LEGACY_ARMOR_ITEMS_TAG, LEGACY_ARMOR_DROPS_TAG, 0);
        migrateLegacySlot(tag, equipment, dropChances, "legs", LEGACY_ARMOR_ITEMS_TAG, LEGACY_ARMOR_DROPS_TAG, 1);
        migrateLegacySlot(tag, equipment, dropChances, "chest", LEGACY_ARMOR_ITEMS_TAG, LEGACY_ARMOR_DROPS_TAG, 2);
        migrateLegacySlot(tag, equipment, dropChances, "head", LEGACY_ARMOR_ITEMS_TAG, LEGACY_ARMOR_DROPS_TAG, 3);

        if (equipment.isEmpty()) {
            tag.remove(EQUIPMENT_TAG);
        } else {
            tag.put(EQUIPMENT_TAG, equipment);
        }
        if (dropChances.isEmpty()) {
            tag.remove(DROP_CHANCES_TAG);
        } else {
            tag.put(DROP_CHANCES_TAG, dropChances);
        }

        tag.remove(LEGACY_HAND_ITEMS_TAG);
        tag.remove(LEGACY_HAND_DROPS_TAG);
        tag.remove(LEGACY_ARMOR_ITEMS_TAG);
        tag.remove(LEGACY_ARMOR_DROPS_TAG);
    }

    private static void normalizeVillagerTradeData(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return;
        }
        CompoundTag villagerData = tag.getCompound(VILLAGER_DATA_TAG).orElse(null);
        boolean hasOffers = hasCustomOffers(tag);
        if (villagerData == null) {
            if (!hasOffers) {
                return;
            }
            villagerData = new CompoundTag();
        }

        String profession = normalizeId(villagerData.getStringOr("profession", ""), NONE_PROFESSION);
        if (hasOffers && NONE_PROFESSION.equals(profession)) {
            profession = DEFAULT_TRADING_PROFESSION;
        }
        villagerData.putString("profession", profession);
        villagerData.putString("type", normalizeId(villagerData.getStringOr("type", ""), DEFAULT_VILLAGER_TYPE));
        villagerData.putInt("level", clampVillagerLevel(villagerData.getIntOr("level", 1)));
        tag.put(VILLAGER_DATA_TAG, villagerData);

        if (hasOffers || !NONE_PROFESSION.equals(profession)) {
            tag.putBoolean(ASSIGN_PROFESSION_WHEN_SPAWNED_TAG, false);
        }
        if (!NONE_PROFESSION.equals(profession) && !hasOffers && tag.getIntOr(XP_TAG, 0) < MIN_LOCKED_PROFESSION_XP) {
            tag.putInt(XP_TAG, MIN_LOCKED_PROFESSION_XP);
        }
    }

    static void enforceVillagerLevelFallbackOnProfessionChange(CompoundTag current, CompoundTag baseline) {
        if (current == null || current.isEmpty()) {
            return;
        }
        CompoundTag currentVillagerData = current.getCompound(VILLAGER_DATA_TAG).orElse(null);
        if (currentVillagerData == null) {
            return;
        }
        String currentProfession = normalizeId(currentVillagerData.getStringOr("profession", ""), NONE_PROFESSION);
        if (NONE_PROFESSION.equals(currentProfession)) {
            return;
        }

        CompoundTag baselineVillagerData = baseline == null ? null : baseline.getCompound(VILLAGER_DATA_TAG).orElse(null);
        String baselineProfession = baselineVillagerData == null
                ? NONE_PROFESSION
                : normalizeId(baselineVillagerData.getStringOr("profession", ""), NONE_PROFESSION);

        if (currentProfession.equals(baselineProfession)) {
            return;
        }

        int level = clampVillagerLevel(currentVillagerData.getIntOr("level", 1));
        if (level < MIN_STABLE_PROFESSION_LEVEL) {
            currentVillagerData.putInt("level", MIN_STABLE_PROFESSION_LEVEL);
            current.put(VILLAGER_DATA_TAG, currentVillagerData);
        }
    }

    private static boolean hasCustomOffers(CompoundTag root) {
        CompoundTag offers = root.getCompound(OFFERS_TAG).orElse(null);
        if (offers == null) {
            return false;
        }
        ListTag recipes = offers.getList(RECIPES_TAG).orElse(null);
        return recipes != null && !recipes.isEmpty();
    }

    private static String normalizeId(String value, String defaultValue) {
        String result = value == null || value.isBlank() ? defaultValue : value;
        if (!result.contains(":")) {
            result = "minecraft:" + result;
        }
        return result;
    }

    private static int clampVillagerLevel(int level) {
        if (level < 1) {
            return 1;
        }
        if (level > 5) {
            return 5;
        }
        return level;
    }

    private static void migrateLegacySlot(
            CompoundTag root,
            CompoundTag equipment,
            CompoundTag dropChances,
            String slotKey,
            String legacyItemsKey,
            String legacyDropsKey,
            int index
    ) {
        if (!equipment.contains(slotKey)) {
            CompoundTag legacyItem = readLegacyItem(root, legacyItemsKey, index);
            if (legacyItem != null && !legacyItem.isEmpty()) {
                equipment.put(slotKey, legacyItem);
            }
        }
        if (!dropChances.contains(slotKey)) {
            Float chance = readLegacyDropChance(root, legacyDropsKey, index);
            if (chance != null
                    && Float.isFinite(chance)
                    && chance >= 0f
                    && chance <= 1f
                    && Math.abs(chance - DEFAULT_DROP_CHANCE) > DROP_EPSILON) {
                dropChances.putFloat(slotKey, chance);
            }
        }
    }

    private static CompoundTag readLegacyItem(CompoundTag root, String key, int index) {
        ListTag list = root.getList(key).orElse(null);
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        Tag element = list.get(index);
        if (!(element instanceof CompoundTag compound)) {
            return null;
        }
        return compound.copy();
    }

    private static Float readLegacyDropChance(CompoundTag root, String key, int index) {
        ListTag list = root.getList(key).orElse(null);
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        Tag element = list.get(index);
        if (element instanceof FloatTag floatTag) {
            return floatTag.floatValue();
        }
        return null;
    }
}
