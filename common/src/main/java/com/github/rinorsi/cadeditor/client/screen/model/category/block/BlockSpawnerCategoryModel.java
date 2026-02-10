package com.github.rinorsi.cadeditor.client.screen.model.category.block;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.client.screen.model.BlockEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class BlockSpawnerCategoryModel extends BlockEditorCategoryModel {
    private static final String KEY_SPAWN_DATA_SNAKE = "spawn_data";
    private static final String KEY_SPAWN_DATA_LEGACY = "SpawnData";
    private static final String[] ENTITY_KEYS = {"entity", "Entity"};

    private static final String KEY_DELAY_SNAKE = "delay";
    private static final String KEY_DELAY_LEGACY = "Delay";
    private static final String KEY_MIN_DELAY_SNAKE = "min_spawn_delay";
    private static final String KEY_MIN_DELAY_LEGACY = "MinSpawnDelay";
    private static final String KEY_MAX_DELAY_SNAKE = "max_spawn_delay";
    private static final String KEY_MAX_DELAY_LEGACY = "MaxSpawnDelay";
    private static final String KEY_SPAWN_COUNT_SNAKE = "spawn_count";
    private static final String KEY_SPAWN_COUNT_LEGACY = "SpawnCount";
    private static final String KEY_MAX_NEARBY_SNAKE = "max_nearby_entities";
    private static final String KEY_MAX_NEARBY_LEGACY = "MaxNearbyEntities";
    private static final String KEY_REQUIRED_PLAYER_RANGE_SNAKE = "required_player_range";
    private static final String KEY_REQUIRED_PLAYER_RANGE_LEGACY = "RequiredPlayerRange";
    private static final String KEY_SPAWN_RANGE_SNAKE = "spawn_range";
    private static final String KEY_SPAWN_RANGE_LEGACY = "SpawnRange";
    private static final String KEY_SPAWN_POTENTIALS_SNAKE = "spawn_potentials";
    private static final String KEY_SPAWN_POTENTIALS_LEGACY = "SpawnPotentials";
    private static final String KEY_NEXT_SPAWN_DATA_SNAKE = "next_spawn_data";
    private static final String KEY_NEXT_SPAWN_DATA_LEGACY = "NextSpawnData";

    private EntityEntryModel entityEntry;
    private IntegerEntryModel delayEntry;
    private IntegerEntryModel minSpawnDelayEntry;
    private IntegerEntryModel maxSpawnDelayEntry;
    private IntegerEntryModel spawnCountEntry;
    private IntegerEntryModel maxNearbyEntry;
    private IntegerEntryModel requiredPlayerRangeEntry;
    private IntegerEntryModel spawnRangeEntry;
    private CompoundTag spawnDataExtras = new CompoundTag();
    private boolean preferSnakeCase = true;
    private String initialEntityId = "";

    public BlockSpawnerCategoryModel(BlockEditorModel editor) {
        super(ModTexts.gui("spawner"), editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag tag = ensureTag();
        preferSnakeCase = detectSnakeCase(tag);
        CompoundTag rawSpawnData = readCompound(tag, KEY_SPAWN_DATA_SNAKE, KEY_SPAWN_DATA_LEGACY);
        CompoundTag entityData = extractEntityData(rawSpawnData);
        spawnDataExtras = extractSpawnDataExtras(rawSpawnData);
        initialEntityId = normalizeEntityId(entityData.getStringOr("id", ""));
        DebugLog.info(() -> "[SpawnerModel] setup preferSnake=" + preferSnakeCase
                + " initialEntity=" + initialEntityId
                + " hasSpawnData=" + !rawSpawnData.isEmpty()
                + " hasPotentials=" + (tag.contains(KEY_SPAWN_POTENTIALS_SNAKE) || tag.contains(KEY_SPAWN_POTENTIALS_LEGACY)));

        EntityType<?> type = resolveEntityType(entityData);
        entityEntry = new EntityEntryModel(this, type, entityData, value -> {
        }).withWeight(0);
        entityEntry.setLabel(ModTexts.gui("spawner_entity"));
        entityEntry.entityIdProperty().addListener(value -> refreshEntityValidity());
        entityEntry.validProperty().addListener(value -> refreshEntityValidity());

        int delay = readInt(tag, 20, KEY_DELAY_SNAKE, KEY_DELAY_LEGACY);
        int minDelay = readInt(tag, 200, KEY_MIN_DELAY_SNAKE, KEY_MIN_DELAY_LEGACY);
        int maxDelay = readInt(tag, 800, KEY_MAX_DELAY_SNAKE, KEY_MAX_DELAY_LEGACY);
        int spawnCount = readInt(tag, 4, KEY_SPAWN_COUNT_SNAKE, KEY_SPAWN_COUNT_LEGACY);
        int maxNearby = readInt(tag, 6, KEY_MAX_NEARBY_SNAKE, KEY_MAX_NEARBY_LEGACY);
        int requiredPlayerRange = readInt(tag, 16, KEY_REQUIRED_PLAYER_RANGE_SNAKE, KEY_REQUIRED_PLAYER_RANGE_LEGACY);
        int spawnRange = readInt(tag, 4, KEY_SPAWN_RANGE_SNAKE, KEY_SPAWN_RANGE_LEGACY);

        delayEntry = new IntegerEntryModel(this, ModTexts.gui("spawner_delay"), Math.max(0, delay), value -> {
        }, value -> value >= 0);
        minSpawnDelayEntry = new IntegerEntryModel(this, ModTexts.gui("spawner_min_delay"), Math.max(0, minDelay), value -> {
        }, value -> value >= 0);
        maxSpawnDelayEntry = new IntegerEntryModel(this, ModTexts.gui("spawner_max_delay"), Math.max(1, maxDelay), value -> {
        }, value -> value >= 1);
        spawnCountEntry = new IntegerEntryModel(this, ModTexts.gui("spawner_count"), Math.max(1, spawnCount), value -> {
        }, value -> value >= 1);
        maxNearbyEntry = new IntegerEntryModel(this, ModTexts.gui("spawner_max_nearby"), Math.max(0, maxNearby), value -> {
        }, value -> value >= 0);
        requiredPlayerRangeEntry = new IntegerEntryModel(this, ModTexts.gui("spawner_required_player_range"),
                Math.max(1, requiredPlayerRange), value -> {
        }, value -> value >= 1);
        spawnRangeEntry = new IntegerEntryModel(this, ModTexts.gui("spawner_spawn_range"), Math.max(0, spawnRange), value -> {
        }, value -> value >= 0);

        getEntries().add(entityEntry);
        getEntries().add(delayEntry);
        getEntries().add(minSpawnDelayEntry);
        getEntries().add(maxSpawnDelayEntry);
        getEntries().add(spawnCountEntry);
        getEntries().add(maxNearbyEntry);
        getEntries().add(requiredPlayerRangeEntry);
        getEntries().add(spawnRangeEntry);
        refreshEntityValidity();
    }

    @Override
    public void apply() {
        super.apply();
        CompoundTag tag = ensureTag();

        int delay = Math.max(0, delayEntry.getValue());
        int minDelay = Math.max(0, minSpawnDelayEntry.getValue());
        int maxDelay = Math.max(1, maxSpawnDelayEntry.getValue());
        if (maxDelay < minDelay) {
            maxDelay = minDelay;
        }
        int spawnCount = Math.max(1, spawnCountEntry.getValue());
        int maxNearby = Math.max(0, maxNearbyEntry.getValue());
        int requiredPlayerRange = Math.max(1, requiredPlayerRangeEntry.getValue());
        int spawnRange = Math.max(0, spawnRangeEntry.getValue());

        writeInt(tag, KEY_DELAY_SNAKE, KEY_DELAY_LEGACY, delay, preferSnakeCase);
        writeInt(tag, KEY_MIN_DELAY_SNAKE, KEY_MIN_DELAY_LEGACY, minDelay, preferSnakeCase);
        writeInt(tag, KEY_MAX_DELAY_SNAKE, KEY_MAX_DELAY_LEGACY, maxDelay, preferSnakeCase);
        writeInt(tag, KEY_SPAWN_COUNT_SNAKE, KEY_SPAWN_COUNT_LEGACY, spawnCount, preferSnakeCase);
        writeInt(tag, KEY_MAX_NEARBY_SNAKE, KEY_MAX_NEARBY_LEGACY, maxNearby, preferSnakeCase);
        writeInt(tag, KEY_REQUIRED_PLAYER_RANGE_SNAKE, KEY_REQUIRED_PLAYER_RANGE_LEGACY, requiredPlayerRange, preferSnakeCase);
        writeInt(tag, KEY_SPAWN_RANGE_SNAKE, KEY_SPAWN_RANGE_LEGACY, spawnRange, preferSnakeCase);

        CompoundTag entity = sanitizeEntityData(entityEntry.copyValue());
        String selectedEntityId = normalizeEntityId(entity.getStringOr("id", ""));
        boolean entityChanged = !selectedEntityId.equals(initialEntityId);
        if (entity.isEmpty() || !entity.contains("id")) {
            tag.remove(KEY_SPAWN_DATA_SNAKE);
            tag.remove(KEY_SPAWN_DATA_LEGACY);
        } else {
            CompoundTag spawnData = spawnDataExtras == null ? new CompoundTag() : spawnDataExtras.copy();
            spawnData.put("entity", entity);
            spawnData.remove("Entity");
            writeCompound(tag, KEY_SPAWN_DATA_SNAKE, KEY_SPAWN_DATA_LEGACY, spawnData, preferSnakeCase);
        }

        if (entityChanged) {
            tag.remove(KEY_SPAWN_POTENTIALS_SNAKE);
            tag.remove(KEY_SPAWN_POTENTIALS_LEGACY);
            tag.remove(KEY_NEXT_SPAWN_DATA_SNAKE);
            tag.remove(KEY_NEXT_SPAWN_DATA_LEGACY);
        }
        DebugLog.info(() -> "[SpawnerModel] apply selectedEntity=" + selectedEntityId
                + " changed=" + entityChanged
                + " preferSnake=" + preferSnakeCase
                + " hasSpawnDataSnake=" + tag.contains(KEY_SPAWN_DATA_SNAKE)
                + " hasSpawnDataLegacy=" + tag.contains(KEY_SPAWN_DATA_LEGACY)
                + " hasPotentials=" + (tag.contains(KEY_SPAWN_POTENTIALS_SNAKE) || tag.contains(KEY_SPAWN_POTENTIALS_LEGACY)));

        initialEntityId = selectedEntityId;
        getContext().setTag(tag);
    }

    private CompoundTag ensureTag() {
        CompoundTag tag = getData();
        if (tag == null) {
            tag = new CompoundTag();
            getContext().setTag(tag);
        }
        return tag;
    }

    private static EntityType<?> resolveEntityType(CompoundTag entityData) {
        if (entityData == null || entityData.isEmpty()) {
            return null;
        }
        String id = entityData.getStringOr("id", "");
        Identifier location = ClientUtil.parseResourceLocation(id);
        return location == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(location).orElse(null);
    }

    private static CompoundTag extractEntityData(CompoundTag spawnData) {
        if (spawnData == null || spawnData.isEmpty()) {
            return new CompoundTag();
        }
        for (String key : ENTITY_KEYS) {
            if (spawnData.contains(key)) {
                return spawnData.getCompound(key).map(CompoundTag::copy).orElseGet(CompoundTag::new);
            }
        }
        if (looksLikeEntityData(spawnData)) {
            return spawnData.copy();
        }
        return new CompoundTag();
    }

    private static CompoundTag extractSpawnDataExtras(CompoundTag spawnData) {
        if (spawnData == null || spawnData.isEmpty()) {
            return new CompoundTag();
        }
        if (looksLikeEntityData(spawnData) && !spawnData.contains("entity") && !spawnData.contains("Entity")) {
            return new CompoundTag();
        }
        CompoundTag extras = spawnData.copy();
        extras.remove("entity");
        extras.remove("Entity");
        return extras;
    }

    private static boolean looksLikeEntityData(CompoundTag tag) {
        return tag.contains("id") || tag.contains("Pos") || tag.contains("Health") || tag.contains("Passengers");
    }

    private static CompoundTag sanitizeEntityData(CompoundTag entityData) {
        if (entityData == null || entityData.isEmpty()) {
            return new CompoundTag();
        }
        CompoundTag sanitized = entityData.copy();
        String id = sanitized.getStringOr("id", "").trim();
        if (id.isEmpty()) {
            sanitized.remove("id");
            return sanitized;
        }
        Identifier parsed = ClientUtil.parseResourceLocation(id);
        if (parsed != null) {
            sanitized.putString("id", parsed.toString());
        } else {
            sanitized.putString("id", id);
        }
        return sanitized;
    }

    private static int readInt(CompoundTag tag, int fallback, String primaryKey, String secondaryKey) {
        if (tag == null) {
            return fallback;
        }
        if (tag.contains(primaryKey)) {
            return tag.getIntOr(primaryKey, fallback);
        }
        if (tag.contains(secondaryKey)) {
            return tag.getIntOr(secondaryKey, fallback);
        }
        return fallback;
    }

    private static CompoundTag readCompound(CompoundTag tag, String primaryKey, String secondaryKey) {
        if (tag == null) {
            return new CompoundTag();
        }
        if (tag.contains(primaryKey)) {
            return tag.getCompound(primaryKey).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        }
        if (tag.contains(secondaryKey)) {
            return tag.getCompound(secondaryKey).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        }
        return new CompoundTag();
    }

    private static void writeInt(CompoundTag tag, String snakeKey, String legacyKey, int value, boolean useSnakeKey) {
        if (tag == null) {
            return;
        }
        String target = useSnakeKey ? snakeKey : legacyKey;
        String other = useSnakeKey ? legacyKey : snakeKey;
        tag.putInt(target, value);
        tag.remove(other);
    }

    private static void writeCompound(CompoundTag tag, String snakeKey, String legacyKey, CompoundTag value, boolean useSnakeKey) {
        if (tag == null) {
            return;
        }
        String target = useSnakeKey ? snakeKey : legacyKey;
        String other = useSnakeKey ? legacyKey : snakeKey;
        tag.put(target, value);
        tag.remove(other);
    }

    private static boolean detectSnakeCase(CompoundTag tag) {
        if (tag == null) {
            return true;
        }
        if (tag.contains(KEY_SPAWN_DATA_SNAKE) || tag.contains(KEY_DELAY_SNAKE) || tag.contains(KEY_MIN_DELAY_SNAKE)
                || tag.contains(KEY_MAX_DELAY_SNAKE) || tag.contains(KEY_SPAWN_COUNT_SNAKE)) {
            return true;
        }
        if (tag.contains(KEY_SPAWN_DATA_LEGACY) || tag.contains(KEY_DELAY_LEGACY) || tag.contains(KEY_MIN_DELAY_LEGACY)
                || tag.contains(KEY_MAX_DELAY_LEGACY) || tag.contains(KEY_SPAWN_COUNT_LEGACY)) {
            return false;
        }
        return true;
    }

    private static String normalizeEntityId(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        Identifier parsed = ClientUtil.parseResourceLocation(trimmed);
        return parsed == null ? trimmed : parsed.toString();
    }

    private void refreshEntityValidity() {
        String id = entityEntry.getEntityId();
        if (id == null || id.isBlank()) {
            entityEntry.setValid(true);
        }
    }
}
