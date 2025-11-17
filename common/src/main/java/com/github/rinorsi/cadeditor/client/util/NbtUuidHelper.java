package com.github.rinorsi.cadeditor.client.util;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Bridges the removed CompoundTag UUID helpers between 1.21.4 and 1.21.8. Mojang now exposes UUID
 * data through Optional-based getters, so we encode/decode using the legacy int[] format to remain
 * compatible with existing saves.
 */
public final class NbtUuidHelper {
    private NbtUuidHelper() {}

    public static UUID getUuid(CompoundTag tag, String key) {
        if (tag == null || key == null || !tag.contains(key)) {
            return null;
        }
        return tag.getIntArray(key)
                .filter(arr -> arr.length == 4)
                .map(NbtUuidHelper::uuidFromIntArray)
                .or(() -> tag.getString(key).map(NbtUuidHelper::parseUuidString))
                .orElse(null);
    }

    public static void putUuid(CompoundTag tag, String key, UUID value) {
        if (tag == null || key == null || value == null) {
            return;
        }
        tag.putIntArray(key, uuidToIntArray(value));
    }

    private static UUID parseUuidString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static int[] uuidToIntArray(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        return new int[]{
                (int) (most >> 32),
                (int) most,
                (int) (least >> 32),
                (int) least
        };
    }

    private static UUID uuidFromIntArray(int[] data) {
        long most = ((long) data[0] << 32) | (data[1] & 0xffffffffL);
        long least = ((long) data[2] << 32) | (data[3] & 0xffffffffL);
        return new UUID(most, least);
    }
}
