package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemHideFlagsCategoryModel;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

class ItemEditorModelMigrationTest {

    @BeforeAll
    static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void legacyEnchantmentsAreConvertedToComponents() {
        CompoundTag legacyTag = new CompoundTag();
        ListTag enchantments = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putString("id", "minecraft:sharpness");
        entry.putInt("lvl", 3);
        enchantments.add(entry);
        legacyTag.put("Enchantments", enchantments);

        CompoundTag oldRoot = new CompoundTag();
        oldRoot.putString("id", BuiltInRegistries.ITEM.getKey(Items.DIAMOND_SWORD).toString());
        oldRoot.put("tag", legacyTag.copy());

        CompoundTag merged = ItemEditorModel.mergeComponentsPreservingUnknown(oldRoot, oldRoot, Collections.emptySet());
        CompoundTag components = requireCompound(merged, "components");
        CompoundTag enchantComponent = requireCompound(components, "minecraft:enchantments");
        CompoundTag levels = requireCompound(enchantComponent, "levels");
        Assertions.assertEquals(3, levels.getInt("minecraft:sharpness").orElse(0));

        CompoundTag migratedLegacy = merged.getCompound("tag").orElse(new CompoundTag());
        Assertions.assertFalse(migratedLegacy.contains("Enchantments"), "Legacy enchantment list should be removed");
    }

    @Test
    void legacyHideFlagsProduceTooltipDisplayAndShowFlags() {
        CompoundTag legacyTag = new CompoundTag();
        int hideMask = ItemHideFlagsCategoryModel.HideFlag.ENCHANTMENTS.getValue()
                | ItemHideFlagsCategoryModel.HideFlag.LORE.getValue();
        legacyTag.putInt("HideFlags", hideMask);

        CompoundTag components = new CompoundTag();
        CompoundTag enchantComponent = new CompoundTag();
        CompoundTag levels = new CompoundTag();
        levels.putInt("minecraft:sharpness", 2);
        enchantComponent.put("levels", levels);
        components.put("minecraft:enchantments", enchantComponent);

        CompoundTag root = new CompoundTag();
        root.putString("id", BuiltInRegistries.ITEM.getKey(Items.DIAMOND_SWORD).toString());
        root.put("tag", legacyTag.copy());
        root.put("components", components.copy());

        CompoundTag migrated = ItemEditorModel.mergeComponentsPreservingUnknown(root, root, Collections.emptySet());
        CompoundTag migratedComponents = requireCompound(migrated, "components");
        CompoundTag tooltipDisplay = requireCompound(migratedComponents, "minecraft:tooltip_display");
        Set<String> hiddenIds = readHiddenComponentIds(tooltipDisplay.getList("hidden_components").orElse(new ListTag()));
        Assertions.assertTrue(hiddenIds.contains("minecraft:enchantments"));
        Assertions.assertTrue(hiddenIds.contains("minecraft:lore"));

        CompoundTag migratedEnchant = requireCompound(migratedComponents, "minecraft:enchantments");
        Assertions.assertFalse(migratedEnchant.getBoolean("show_in_tooltip").orElse(true));

        CompoundTag migratedLegacy = migrated.getCompound("tag").orElse(new CompoundTag());
        Assertions.assertFalse(migratedLegacy.contains("HideFlags"), "Legacy HideFlags key should be removed");
    }

    @Test
    void legacyHideTooltipComponentSetsTooltipDisplay() {
        CompoundTag components = new CompoundTag();
        components.put("minecraft:hide_tooltip", new CompoundTag());

        CompoundTag root = new CompoundTag();
        root.putString("id", BuiltInRegistries.ITEM.getKey(Items.BOOK).toString());
        root.put("components", components.copy());

        CompoundTag migrated = ItemEditorModel.mergeComponentsPreservingUnknown(root, root, Collections.emptySet());
        CompoundTag migratedComponents = requireCompound(migrated, "components");
        CompoundTag tooltipDisplay = requireCompound(migratedComponents, "minecraft:tooltip_display");
        Assertions.assertTrue(tooltipDisplay.getBoolean("hide_tooltip").orElse(false));
        Assertions.assertFalse(migratedComponents.contains("minecraft:hide_tooltip"), "Legacy hide tooltip component should be cleared");
    }

    private static CompoundTag requireCompound(CompoundTag parent, String key) {
        return parent.getCompound(key)
                .orElseThrow(() -> new AssertionError("Missing compound: " + key));
    }

    private static Set<String> readHiddenComponentIds(ListTag list) {
        return list.stream()
                .filter(StringTag.class::isInstance)
                .map(StringTag.class::cast)
                .map(StringTag::value)
                .collect(Collectors.toSet());
    }
}
