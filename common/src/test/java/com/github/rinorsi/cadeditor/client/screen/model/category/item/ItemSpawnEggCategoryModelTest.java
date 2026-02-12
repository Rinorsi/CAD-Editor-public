package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ItemSpawnEggCategoryModelTest {

    @Test
    void sanitizePayloadRemovesTransientFieldsAndZeroHealth() {
        CompoundTag payload = new CompoundTag();
        payload.putString("id", "allay");
        payload.put("Pos", new ListTag());
        payload.put("Motion", new ListTag());
        payload.put("Rotation", new ListTag());
        payload.putIntArray("UUID", new int[]{1, 2, 3, 4});
        payload.putLong("UUIDMost", 123L);
        payload.putLong("UUIDLeast", 456L);
        payload.putFloat("Health", 0.0f);
        payload.putBoolean("NoAI", true);

        CompoundTag sanitized = ItemSpawnEggCategoryModel.sanitizeSpawnEggComponentPayload(payload);

        Assertions.assertEquals("minecraft:allay", sanitized.getString("id").orElse(""));
        Assertions.assertFalse(sanitized.contains("Pos"));
        Assertions.assertFalse(sanitized.contains("Motion"));
        Assertions.assertFalse(sanitized.contains("Rotation"));
        Assertions.assertFalse(sanitized.contains("UUID"));
        Assertions.assertFalse(sanitized.contains("UUIDMost"));
        Assertions.assertFalse(sanitized.contains("UUIDLeast"));
        Assertions.assertFalse(sanitized.contains("Health"));
        Assertions.assertTrue(sanitized.getBoolean("NoAI").orElse(false));
    }

    @Test
    void sanitizePayloadKeepsPositiveHealthAndCustomFields() {
        CompoundTag payload = new CompoundTag();
        payload.putString("id", "minecraft:cow");
        payload.putFloat("Health", 20.0f);
        payload.putString("CustomName", "{\"text\":\"moo\"}");

        CompoundTag sanitized = ItemSpawnEggCategoryModel.sanitizeSpawnEggComponentPayload(payload);

        Assertions.assertEquals("minecraft:cow", sanitized.getString("id").orElse(""));
        Assertions.assertEquals(20.0f, sanitized.getFloat("Health").orElse(0f), 1e-6f);
        Assertions.assertEquals("{\"text\":\"moo\"}", sanitized.getString("CustomName").orElse(""));
    }

    @Test
    void professionChangeFallbackPromotesLevelTo2() {
        CompoundTag current = new CompoundTag();
        CompoundTag currentVillagerData = new CompoundTag();
        currentVillagerData.putString("profession", "minecraft:librarian");
        currentVillagerData.putInt("level", 1);
        current.put("VillagerData", currentVillagerData);

        CompoundTag baseline = new CompoundTag();
        CompoundTag baselineVillagerData = new CompoundTag();
        baselineVillagerData.putString("profession", "minecraft:none");
        baselineVillagerData.putInt("level", 1);
        baseline.put("VillagerData", baselineVillagerData);

        ItemSpawnEggCategoryModel.enforceVillagerLevelFallbackOnProfessionChange(current, baseline);

        int level = current.getCompound("VillagerData")
                .flatMap(data -> data.getInt("level"))
                .orElse(0);
        Assertions.assertEquals(2, level);
    }

    @Test
    void professionFallbackDoesNotTriggerWithoutProfessionChange() {
        CompoundTag current = new CompoundTag();
        CompoundTag currentVillagerData = new CompoundTag();
        currentVillagerData.putString("profession", "minecraft:librarian");
        currentVillagerData.putInt("level", 1);
        current.put("VillagerData", currentVillagerData);

        CompoundTag baseline = new CompoundTag();
        CompoundTag baselineVillagerData = new CompoundTag();
        baselineVillagerData.putString("profession", "minecraft:librarian");
        baselineVillagerData.putInt("level", 1);
        baseline.put("VillagerData", baselineVillagerData);

        ItemSpawnEggCategoryModel.enforceVillagerLevelFallbackOnProfessionChange(current, baseline);

        int level = current.getCompound("VillagerData")
                .flatMap(data -> data.getInt("level"))
                .orElse(0);
        Assertions.assertEquals(1, level);
    }
}
