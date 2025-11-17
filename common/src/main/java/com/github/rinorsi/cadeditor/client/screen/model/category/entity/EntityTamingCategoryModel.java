package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtUuidHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
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
        boolean tame = data.getBooleanOr("Tame", false);
        boolean sitting = data.getBooleanOr("Sitting", false);
        boolean inSittingPose = data.getBooleanOr("InSittingPose", false);

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
        String direct = data.getString("Owner").orElse("");
        if (!direct.isBlank() && !isUuidString(direct)) {
            return direct;
        }
        return data.getString("OwnerName").orElse("");
    }

    private String readOwnerUuid(CompoundTag data) {
        UUID uuid = NbtUuidHelper.getUuid(data, "OwnerUUID");
        if (uuid != null) {
            return uuid.toString();
        }
        uuid = NbtUuidHelper.getUuid(data, "Owner");
        if (uuid != null) {
            return uuid.toString();
        }
        if (data.getLong("OwnerUUIDMost").isPresent() && data.getLong("OwnerUUIDLeast").isPresent()) {
            long most = data.getLongOr("OwnerUUIDMost", 0L);
            long least = data.getLongOr("OwnerUUIDLeast", 0L);
            return new UUID(most, least).toString();
        }
        String ownerUuid = data.getString("OwnerUUID").orElse("");
        if (!ownerUuid.isBlank()) {
            return ownerUuid;
        }
        String owner = data.getString("Owner").orElse("");
        if (isUuidString(owner)) {
            return owner;
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
            String owner = data.getString("Owner").orElse("");
            if (!owner.isEmpty() && !isUuidString(owner)) {
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
            NbtUuidHelper.putUuid(data, "OwnerUUID", uuid);
            NbtUuidHelper.putUuid(data, "Owner", uuid);
        } else {
            data.putString("OwnerUUID", ownerUuid);
        }
    }

    private void removeOwnerUuid(CompoundTag data) {
        data.remove("OwnerUUID");
        data.remove("OwnerUUIDMost");
        data.remove("OwnerUUIDLeast");
        String owner = data.getString("Owner").orElse("");
        if (isUuidString(owner) || owner.isBlank()) {
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
