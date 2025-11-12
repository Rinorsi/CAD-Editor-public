package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class EntityTamingCategoryModel extends EntityCategoryModel {
    private BooleanEntryModel tameEntry;
    private BooleanEntryModel sittingEntry;
    private BooleanEntryModel sittingPoseEntry;
    private StringEntryModel ownerNameEntry;
    private StringEntryModel ownerUuidEntry;

    public EntityTamingCategoryModel(EntityEditorModel model) {
        super(ModTexts.ENTITY_TAMING, model);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = getData();
        if (data == null) {
            return;
        }
        boolean tame = data.getBoolean("Tame");
        boolean sitting = data.getBoolean("Sitting");
        boolean inSittingPose = data.getBoolean("InSittingPose");

        String ownerName = readOwnerName(data);
        String ownerUuid = readOwnerUuid(data);

        tameEntry = new BooleanEntryModel(this, ModTexts.TAME, tame, value -> {});
        ownerNameEntry = new StringEntryModel(this, ModTexts.OWNER_NAME, ownerName, value -> {});
        ownerUuidEntry = new StringEntryModel(this, ModTexts.OWNER_UUID, ownerUuid, value -> {});
        sittingEntry = new BooleanEntryModel(this, ModTexts.SITTING, sitting, value -> {});
        sittingPoseEntry = new BooleanEntryModel(this, ModTexts.IN_SITTING_POSE, inSittingPose, value -> {});

        getEntries().add(tameEntry);
        getEntries().add(ownerNameEntry);
        getEntries().add(ownerUuidEntry);
        getEntries().add(new ActionEntryModel(this, ModTexts.USE_SELF_UUID, this::useClientUuid));
        getEntries().add(sittingEntry);
        getEntries().add(sittingPoseEntry);
    }

    private void useClientUuid() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        UUID uuid = player.getUUID();
        ownerUuidEntry.setValue(uuid.toString());
        tameEntry.setValue(true);
    }

    private String readOwnerName(CompoundTag data) {
        if (data.contains("Owner", Tag.TAG_STRING)) {
            String value = data.getString("Owner");
            if (!isUuidString(value)) {
                return value;
            }
        }
        if (data.contains("OwnerName", Tag.TAG_STRING)) {
            return data.getString("OwnerName");
        }
        return "";
    }

    private String readOwnerUuid(CompoundTag data) {
        if (data.hasUUID("OwnerUUID")) {
            return data.getUUID("OwnerUUID").toString();
        }
        if (data.hasUUID("Owner")) {
            return data.getUUID("Owner").toString();
        }
        if (data.contains("OwnerUUIDMost", Tag.TAG_LONG) && data.contains("OwnerUUIDLeast", Tag.TAG_LONG)) {
            return new UUID(data.getLong("OwnerUUIDMost"), data.getLong("OwnerUUIDLeast")).toString();
        }
        if (data.contains("OwnerUUID", Tag.TAG_STRING)) {
            return data.getString("OwnerUUID");
        }
        if (data.contains("Owner", Tag.TAG_STRING)) {
            String raw = data.getString("Owner");
            if (isUuidString(raw)) {
                return raw;
            }
        }
        return "";
    }

    private static boolean isUuidString(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    @Override
    public void apply() {
        super.apply();
        CompoundTag data = getData();
        if (data == null) {
            return;
        }

        boolean tame = Boolean.TRUE.equals(tameEntry.getValue());
        String ownerName = normalize(ownerNameEntry.getValue());
        String ownerUuid = normalize(ownerUuidEntry.getValue());
        boolean hasOwnerData = !ownerName.isEmpty() || !ownerUuid.isEmpty();

        if (!hasOwnerData) {
            tame = false;
        }

        applyTameState(data, tame);
        applyOwnerName(data, ownerName);
        applyOwnerUuid(data, ownerUuid);

        if (!tame) {
            removeBooleanTag(data, "Sitting");
            removeBooleanTag(data, "InSittingPose");
        } else {
            applyBooleanTag(data, "Sitting", Boolean.TRUE.equals(sittingEntry.getValue()));
            applyBooleanTag(data, "InSittingPose", Boolean.TRUE.equals(sittingPoseEntry.getValue()));
        }
    }

    private void applyTameState(CompoundTag data, boolean tame) {
        if (tame) {
            data.putBoolean("Tame", true);
        } else {
            data.remove("Tame");
        }
    }

    private void applyOwnerName(CompoundTag data, String ownerName) {
        if (ownerName.isEmpty()) {
            data.remove("OwnerName");
            if (data.contains("Owner", Tag.TAG_STRING) && !isUuidString(data.getString("Owner"))) {
                data.remove("Owner");
            }
            return;
        }
        data.putString("Owner", ownerName);
        data.putString("OwnerName", ownerName);
    }

    private void applyOwnerUuid(CompoundTag data, String ownerUuid) {
        removeOwnerUuid(data);
        if (ownerUuid.isEmpty()) {
            return;
        }
        if (isUuidString(ownerUuid)) {
            UUID uuid = UUID.fromString(ownerUuid);
            data.putUUID("OwnerUUID", uuid);
            data.putUUID("Owner", uuid);
        } else {
            data.putString("OwnerUUID", ownerUuid);
        }
    }

    private void removeOwnerUuid(CompoundTag data) {
        if (data.hasUUID("OwnerUUID") || data.contains("OwnerUUID", Tag.TAG_STRING)) {
            data.remove("OwnerUUID");
        }
        if (data.contains("OwnerUUIDMost", Tag.TAG_LONG)) {
            data.remove("OwnerUUIDMost");
        }
        if (data.contains("OwnerUUIDLeast", Tag.TAG_LONG)) {
            data.remove("OwnerUUIDLeast");
        }
        if (data.hasUUID("Owner") || (data.contains("Owner", Tag.TAG_STRING) && isUuidString(data.getString("Owner")))) {
            data.remove("Owner");
        }
    }

    private void applyBooleanTag(CompoundTag data, String key, boolean value) {
        if (value) {
            data.putBoolean(key, true);
        } else {
            removeBooleanTag(data, key);
        }
    }

    private void removeBooleanTag(CompoundTag data, String key) {
        if (data.contains(key)) {
            data.remove(key);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
