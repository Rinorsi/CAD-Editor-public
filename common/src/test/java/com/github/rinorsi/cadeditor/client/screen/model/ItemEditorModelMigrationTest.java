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

    @Test
    void legacyAttributeModifiersAreConvertedToComponent() {
        CompoundTag legacyTag = new CompoundTag();
        ListTag attributeModifiers = new ListTag();
        CompoundTag modifier = new CompoundTag();
        modifier.putString("AttributeName", "minecraft:attack_damage");
        modifier.putDouble("Amount", 10.5d);
        modifier.putInt("Operation", 0);
        modifier.putString("Slot", "mainhand");
        attributeModifiers.add(modifier);
        legacyTag.put("AttributeModifiers", attributeModifiers);

        CompoundTag root = new CompoundTag();
        root.putString("id", BuiltInRegistries.ITEM.getKey(Items.DIAMOND_SWORD).toString());
        root.put("tag", legacyTag.copy());

        CompoundTag migrated = ItemEditorModel.mergeComponentsPreservingUnknown(root, root, Collections.emptySet());
        CompoundTag components = requireCompound(migrated, "components");
        CompoundTag attributeComponent = requireCompound(components, "minecraft:attribute_modifiers");
        ListTag modifiers = attributeComponent.getList("modifiers").orElse(new ListTag());
        Assertions.assertEquals(1, modifiers.size());
        CompoundTag migratedModifier = modifiers.getCompound(0).orElseThrow();
        Assertions.assertEquals("minecraft:attack_damage", migratedModifier.getString("type").orElse(""));
        Assertions.assertEquals(10.5d, migratedModifier.getDouble("amount").orElse(0d), 1e-6);
        Assertions.assertEquals("add_value", migratedModifier.getString("operation").orElse(""));
        Assertions.assertEquals("mainhand", migratedModifier.getString("slot").orElse(""));
        Assertions.assertTrue(migratedModifier.contains("id"));
        String migratedId = migratedModifier.getString("id").orElse("");
        Assertions.assertTrue(migratedId.startsWith("cadeditor:m_"), "Modifier id should use compact cadeditor namespace");
        Assertions.assertTrue(migratedId.length() <= 32, "Modifier id should stay compact");

        CompoundTag migratedLegacy = migrated.getCompound("tag").orElse(new CompoundTag());
        Assertions.assertFalse(migratedLegacy.contains("AttributeModifiers"), "Legacy attribute modifiers should be removed");
    }

    @Test
    void legacyCanDestroyAndCanPlaceOnAreConvertedToComponents() {
        CompoundTag legacyTag = new CompoundTag();
        ListTag canDestroy = new ListTag();
        canDestroy.add(StringTag.valueOf("stone"));
        canDestroy.add(StringTag.valueOf("#mineable/pickaxe"));
        legacyTag.put("CanDestroy", canDestroy);

        ListTag canPlaceOn = new ListTag();
        canPlaceOn.add(StringTag.valueOf("minecraft:dirt"));
        legacyTag.put("CanPlaceOn", canPlaceOn);

        CompoundTag root = new CompoundTag();
        root.putString("id", BuiltInRegistries.ITEM.getKey(Items.DIAMOND_PICKAXE).toString());
        root.put("tag", legacyTag.copy());

        CompoundTag migrated = ItemEditorModel.mergeComponentsPreservingUnknown(root, root, Collections.emptySet());
        CompoundTag components = requireCompound(migrated, "components");
        CompoundTag canBreak = requireCompound(components, "minecraft:can_break");
        CompoundTag canPlace = requireCompound(components, "minecraft:can_place_on");

        ListTag canBreakPredicates = canBreak.getList("predicates").orElse(new ListTag());
        Assertions.assertEquals(2, canBreakPredicates.size());
        Set<String> breakSelectors = readBlockSelectors(canBreakPredicates);
        Assertions.assertTrue(breakSelectors.contains("minecraft:stone"));
        Assertions.assertTrue(breakSelectors.contains("#minecraft:mineable/pickaxe"));

        ListTag canPlacePredicates = canPlace.getList("predicates").orElse(new ListTag());
        Assertions.assertEquals(1, canPlacePredicates.size());
        Set<String> placeSelectors = readBlockSelectors(canPlacePredicates);
        Assertions.assertTrue(placeSelectors.contains("minecraft:dirt"));

        CompoundTag migratedLegacy = migrated.getCompound("tag").orElse(new CompoundTag());
        Assertions.assertFalse(migratedLegacy.contains("CanDestroy"));
        Assertions.assertFalse(migratedLegacy.contains("CanPlaceOn"));
    }

    @Test
    void legacyEntityTagMovesToEntityDataComponent() {
        CompoundTag legacyTag = new CompoundTag();
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("id", "minecraft:allay");
        legacyTag.put("EntityTag", entityTag);

        CompoundTag root = new CompoundTag();
        root.putString("id", BuiltInRegistries.ITEM.getKey(Items.ALLAY_SPAWN_EGG).toString());
        root.put("tag", legacyTag.copy());

        CompoundTag migrated = ItemEditorModel.mergeComponentsPreservingUnknown(root, root, Collections.emptySet());
        CompoundTag components = requireCompound(migrated, "components");
        CompoundTag entityData = requireCompound(components, "minecraft:entity_data");
        Assertions.assertEquals("minecraft:allay", entityData.getString("id").orElse(""));

        CompoundTag migratedLegacy = migrated.getCompound("tag").orElse(new CompoundTag());
        Assertions.assertFalse(migratedLegacy.contains("EntityTag"));
    }

    @Test
    void legacyPotionFieldsAreConvertedToPotionContentsComponent() {
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putString("Potion", "swiftness");
        legacyTag.putInt("CustomPotionColor", 123456);
        ListTag legacyEffects = new ListTag();
        CompoundTag effect = new CompoundTag();
        effect.putString("Id", "speed");
        effect.putInt("Amplifier", 2);
        effect.putInt("Duration", 1200);
        effect.putBoolean("Ambient", true);
        effect.putBoolean("ShowParticles", false);
        legacyEffects.add(effect);
        legacyTag.put("custom_potion_effects", legacyEffects);

        CompoundTag root = new CompoundTag();
        root.putString("id", BuiltInRegistries.ITEM.getKey(Items.POTION).toString());
        root.put("tag", legacyTag.copy());

        CompoundTag migrated = ItemEditorModel.mergeComponentsPreservingUnknown(root, root, Collections.emptySet());
        CompoundTag components = requireCompound(migrated, "components");
        CompoundTag potionContents = requireCompound(components, "minecraft:potion_contents");
        Assertions.assertEquals("minecraft:swiftness", potionContents.getString("potion").orElse(""));
        Assertions.assertEquals(123456, potionContents.getInt("custom_color").orElse(0));
        ListTag customEffects = potionContents.getList("custom_effects").orElse(new ListTag());
        Assertions.assertEquals(1, customEffects.size());
        CompoundTag migratedEffect = customEffects.getCompound(0).orElseThrow();
        Assertions.assertEquals("minecraft:speed", migratedEffect.getString("id").orElse(""));
        Assertions.assertEquals(2, migratedEffect.getInt("amplifier").orElse(0));
        Assertions.assertEquals(1200, migratedEffect.getInt("duration").orElse(0));
        Assertions.assertTrue(migratedEffect.getBoolean("ambient").orElse(false));
        Assertions.assertFalse(migratedEffect.getBoolean("show_particles").orElse(true));

        CompoundTag migratedLegacy = migrated.getCompound("tag").orElse(new CompoundTag());
        Assertions.assertFalse(migratedLegacy.contains("Potion"));
        Assertions.assertFalse(migratedLegacy.contains("CustomPotionColor"));
        Assertions.assertFalse(migratedLegacy.contains("custom_potion_effects"));
    }

    @Test
    void legacyWritableBookPagesAreConvertedToWritableBookComponent() {
        CompoundTag legacyTag = new CompoundTag();
        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf("page one"));
        pages.add(StringTag.valueOf("page two"));
        legacyTag.put("pages", pages);

        CompoundTag root = new CompoundTag();
        root.putString("id", BuiltInRegistries.ITEM.getKey(Items.WRITABLE_BOOK).toString());
        root.put("tag", legacyTag.copy());

        CompoundTag migrated = ItemEditorModel.mergeComponentsPreservingUnknown(root, root, Collections.emptySet());
        CompoundTag components = requireCompound(migrated, "components");
        CompoundTag writableBook = requireCompound(components, "minecraft:writable_book_content");
        ListTag migratedPages = writableBook.getList("pages").orElse(new ListTag());
        Assertions.assertEquals(2, migratedPages.size());
        Assertions.assertEquals("page one", migratedPages.getString(0).orElse(""));
        Assertions.assertEquals("page two", migratedPages.getString(1).orElse(""));

        CompoundTag migratedLegacy = migrated.getCompound("tag").orElse(new CompoundTag());
        Assertions.assertFalse(migratedLegacy.contains("pages"));
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

    private static Set<String> readBlockSelectors(ListTag predicates) {
        return predicates.stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .map(tag -> tag.getString("blocks").orElse(""))
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
    }
}
