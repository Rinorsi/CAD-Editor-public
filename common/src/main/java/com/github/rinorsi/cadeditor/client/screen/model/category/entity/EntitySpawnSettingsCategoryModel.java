package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;

public class EntitySpawnSettingsCategoryModel extends EntityCategoryModel {
    public EntitySpawnSettingsCategoryModel(EntityEditorModel editor) {
        super(ModTexts.ENTITY_SPAWN, editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = getData();
        getEntries().add(new BooleanEntryModel(this, ModTexts.CAN_PICK_UP_LOOT, data.getBooleanOr("CanPickUpLoot", false), value -> setBoolean("CanPickUpLoot", value)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.PERSISTENCE_REQUIRED, data.getBooleanOr("PersistenceRequired", false), value -> setBoolean("PersistenceRequired", value)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.NO_AI, data.getBooleanOr("NoAI", false), value -> setBoolean("NoAI", value)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.LEFT_HANDED, data.getBooleanOr("LeftHanded", false), value -> setBoolean("LeftHanded", value)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.CAN_JOIN_RAID, data.getBooleanOr("CanJoinRaid", false), value -> setBoolean("CanJoinRaid", value)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.PATROL_LEADER, data.getBooleanOr("PatrolLeader", false), value -> setBoolean("PatrolLeader", value)));

        String team = data.getString("Team").orElse("");
        getEntries().add(new StringEntryModel(this, ModTexts.TEAM, team, this::setTeam));

        float reinforcements = data.getFloatOr("SpawnReinforcementsChance", 0f);
        getEntries().add(new FloatEntryModel(this, ModTexts.SPAWN_REINFORCEMENTS_CHANCE, reinforcements, this::setSpawnReinforcementsChance, value -> value >= 0f && value <= 1f));
    }

    private void setBoolean(String key, boolean value) {
        if (value) {
            getData().putBoolean(key, true);
        } else {
            getData().remove(key);
        }
    }

    private void setTeam(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            getData().remove("Team");
        } else {
            getData().putString("Team", trimmed);
        }
    }

    private void setSpawnReinforcementsChance(float value) {
        float clamped = Math.max(0f, Math.min(1f, value));
        if (clamped > 0f) {
            getData().putFloat("SpawnReinforcementsChance", clamped);
        } else {
            getData().remove("SpawnReinforcementsChance");
        }
    }
}
