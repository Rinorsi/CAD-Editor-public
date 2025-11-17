package com.github.rinorsi.cadeditor.client.util;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * Thin convenience layer over the 1.21.8 Optional-based NBT API so legacy code can keep concise
 * one-liners. Every method in here gracefully handles {@code null} tags and defaults.
 */
public final class NbtHelper {
    private NbtHelper() {
    }

    public static boolean contains(CompoundTag tag, String key) {
        return tag != null && tag.contains(key);
    }

    public static CompoundTag copyOrNew(CompoundTag tag) {
        return tag == null ? new CompoundTag() : tag.copy();
    }

    public static CompoundTag getCompoundOrEmpty(CompoundTag tag, String key) {
        return tag == null ? new CompoundTag() : tag.getCompoundOrEmpty(key);
    }

    public static CompoundTag getCompoundOrNull(CompoundTag tag, String key) {
        return tag == null ? null : tag.getCompound(key).orElse(null);
    }

    public static CompoundTag getOrCreateCompound(CompoundTag tag, String key) {
        if (tag == null) {
            return new CompoundTag();
        }
        return tag.getCompound(key).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            tag.put(key, created);
            return created;
        });
    }

    public static ListTag getListOrEmpty(CompoundTag tag, String key) {
        return tag == null ? new ListTag() : tag.getListOrEmpty(key);
    }

    public static ListTag copyListOrEmpty(CompoundTag tag, String key) {
        return tag == null ? new ListTag() : getListOrEmpty(tag, key).copy();
    }

    public static String getString(CompoundTag tag, String key, String defaultValue) {
        return tag == null ? defaultValue : tag.getStringOr(key, defaultValue);
    }

    public static int getInt(CompoundTag tag, String key, int defaultValue) {
        return tag == null ? defaultValue : tag.getIntOr(key, defaultValue);
    }

    public static long getLong(CompoundTag tag, String key, long defaultValue) {
        return tag == null ? defaultValue : tag.getLongOr(key, defaultValue);
    }

    public static double getDouble(CompoundTag tag, String key, double defaultValue) {
        return tag == null ? defaultValue : tag.getDoubleOr(key, defaultValue);
    }

    public static float getFloat(CompoundTag tag, String key, float defaultValue) {
        return tag == null ? defaultValue : tag.getFloatOr(key, defaultValue);
    }

    public static byte getByte(CompoundTag tag, String key, byte defaultValue) {
        return tag == null ? defaultValue : tag.getByteOr(key, defaultValue);
    }

    public static boolean getBoolean(CompoundTag tag, String key, boolean defaultValue) {
        return tag == null ? defaultValue : tag.getBooleanOr(key, defaultValue);
    }

    public static int[] getIntArray(CompoundTag tag, String key) {
        return tag == null ? new int[0] : tag.getIntArray(key).orElse(new int[0]);
    }

    public static CompoundTag mergeInto(CompoundTag target, String key, CompoundTag value) {
        if (target == null || value == null || isEmptyTag(value)) {
            return target;
        }
        target.put(key, value);
        return target;
    }

    public static void putOrRemove(CompoundTag tag, String key, Tag value) {
        if (tag == null) {
            return;
        }
        if (value == null || isEmptyTag(value)) {
            tag.remove(key);
        } else {
            tag.put(key, value);
        }
    }

    public static double getListDouble(ListTag list, int index, double defaultValue) {
        return list == null ? defaultValue : list.getDouble(index).orElse(defaultValue);
    }

    public static float getListFloat(ListTag list, int index, float defaultValue) {
        return list == null ? defaultValue : list.getFloat(index).orElse(defaultValue);
    }

    private static boolean isEmptyTag(Tag value) {
        if (value instanceof CompoundTag compound) {
            return compound.isEmpty();
        }
        if (value instanceof CollectionTag collection) {
            return collection.isEmpty();
        }
        return false;
    }
}
