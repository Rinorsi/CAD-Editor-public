package com.github.rinorsi.cadeditor.client.screen.model;

import java.util.Objects;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.franckyi.databindings.base.DataBindingsImpl;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

class ItemEditorModelFoodTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        DataBindingsImpl.init();
    }

    //TODO 需要搭建更完整的组件单测/集成测试框架，顺便统计覆盖率
    private static final RegistryAccess.Frozen REGISTRY_ACCESS =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).freeze();

    @Test
    void enablingFoodAddsComponentToContextTag() {
        ItemStack baseStack = new ItemStack(getItem("minecraft:diamond_sword"));
        baseStack.set(DataComponents.CUSTOM_NAME, Component.literal("Delicious"));
        ItemEditorContext context = new ItemEditorContext(baseStack, null, true, noop());
        TestItemEditorModel model = new TestItemEditorModel(context);

        model.initalize();
        model.enableFoodComponent();

        CompoundTag immediateTag = context.getTag();
        Assertions.assertNotNull(immediateTag, "Context tag should be initialized after enabling");
        Assertions.assertTrue(immediateTag.getCompound("components").contains("minecraft:food", Tag.TAG_COMPOUND));

        model.apply();

        ItemStack updated = context.getItemStack();
        var foodComponent = Objects.requireNonNull(
                updated.get(DataComponents.FOOD),
                "Expected food component on context stack"
        );
        Assertions.assertEquals(1.6f, foodComponent.eatSeconds(), 1e-6f);

        CompoundTag tag = context.getTag();
        Assertions.assertNotNull(tag, "Context tag should be initialized");
        Assertions.assertTrue(tag.contains("components", Tag.TAG_COMPOUND), "Components compound missing");

        CompoundTag components = tag.getCompound("components");
        Assertions.assertTrue(components.contains("minecraft:food", Tag.TAG_COMPOUND), "Food component missing in tag");
        CompoundTag food = components.getCompound("minecraft:food");
        Assertions.assertEquals(0, food.getInt("nutrition"));
        Assertions.assertEquals(0.0f, food.getFloat("saturation"));
        Assertions.assertTrue(food.getBoolean("can_always_eat"));
        if (food.contains("eat_seconds", Tag.TAG_FLOAT)) {
            Assertions.assertEquals(1.6f, food.getFloat("eat_seconds"), 1e-6f);
        }
        Assertions.assertFalse(food.contains("using_converts_to", Tag.TAG_COMPOUND));
    }

    @Test
    void disablingFoodAddsRemovalMarkerAndReenablingRestoresComponent() {
        ItemStack baseStack = new ItemStack(getItem("minecraft:diamond_sword"));
        ItemEditorContext context = new ItemEditorContext(baseStack, null, true, noop());
        TestItemEditorModel model = new TestItemEditorModel(context);

        model.initalize();
        model.enableFoodComponent();

        ItemStack enabledStack = context.getItemStack();
        Assertions.assertTrue(enabledStack.has(DataComponents.FOOD), "Stack should become edible after enabling");

        CompoundTag enabledTag = context.getTag();
        Assertions.assertNotNull(enabledTag, "Tag should be initialized when food is enabled");
        CompoundTag enabledComponents = enabledTag.getCompound("components");
        Assertions.assertTrue(enabledComponents.contains("minecraft:food", Tag.TAG_COMPOUND), "Food component missing after enabling");
        Assertions.assertFalse(enabledComponents.contains("!minecraft:food"), "Removal marker should not persist while enabled");

        model.disableFoodComponent();

        CompoundTag disabledTag = context.getTag();
        Assertions.assertNotNull(disabledTag, "Tag should remain available after disabling");
        CompoundTag disabledComponents = disabledTag.getCompound("components");
        Assertions.assertFalse(disabledComponents.contains("minecraft:food", Tag.TAG_COMPOUND), "Food component should be cleared when disabled");
        Assertions.assertTrue(disabledComponents.contains("!minecraft:food", Tag.TAG_COMPOUND), "Removal marker missing when food disabled");

        ItemStack disabledStack = context.getItemStack();
        Assertions.assertFalse(disabledStack.has(DataComponents.FOOD), () -> "Stack should stop being edible after disabling. Tag=" + disabledComponents);

        model.enableFoodComponent();

        ItemStack reenabledStack = context.getItemStack();
        Assertions.assertTrue(reenabledStack.has(DataComponents.FOOD), "Stack should be edible again after re-enabling");

        CompoundTag reenabledTag = context.getTag();
        Assertions.assertNotNull(reenabledTag, "Tag should still be initialized after re-enabling");
        CompoundTag reenabledComponents = reenabledTag.getCompound("components");
        Assertions.assertTrue(reenabledComponents.contains("minecraft:food", Tag.TAG_COMPOUND), "Food component missing after re-enabling");
        Assertions.assertFalse(reenabledComponents.contains("!minecraft:food", Tag.TAG_COMPOUND), "Removal marker should be cleared after re-enabling");
    }

    @Test
    void switchingConvertTargetProducesCleanStack() {
        ItemStack baseStack = new ItemStack(getItem("minecraft:stone_sword"));
        baseStack.set(DataComponents.CUSTOM_NAME, Component.literal("Crunchy"));
        baseStack.remove(DataComponents.LORE);
        baseStack.remove(DataComponents.RARITY);
        baseStack.remove(DataComponents.REPAIR_COST);
        baseStack.set(DataComponents.ATTRIBUTE_MODIFIERS, createSwordModifiers());
        ItemEditorContext context = new ItemEditorContext(baseStack, null, true, noop());
        TestItemEditorModel model = new TestItemEditorModel(context);

        model.initalize();
        model.enableFoodComponent();
        model.getFoodState().setUsingConvertsToId("minecraft:diamond_sword");
        model.apply();

        ItemStack updated = context.getItemStack();
        Objects.requireNonNull(
                updated.get(DataComponents.FOOD),
                "Food component missing after apply"
        );

        CompoundTag tag = context.getTag();
        Assertions.assertNotNull(tag, "Context tag should be initialized");
        CompoundTag components = tag.getCompound("components");
        Assertions.assertTrue(components.contains("!minecraft:lore", Tag.TAG_COMPOUND));
        Assertions.assertTrue(components.contains("!minecraft:rarity", Tag.TAG_COMPOUND));
        Assertions.assertTrue(components.contains("!minecraft:repair_cost", Tag.TAG_COMPOUND));
        CompoundTag food = components.getCompound("minecraft:food");
        CompoundTag convert = food.getCompound("using_converts_to");

        Assertions.assertTrue(components.contains("minecraft:attribute_modifiers", Tag.TAG_COMPOUND));

        Assertions.assertEquals("minecraft:diamond_sword", convert.getString("id"));
        Assertions.assertFalse(convert.contains("components", Tag.TAG_COMPOUND));
    }

    private static Item getItem(String id) {
        ResourceLocation location = ResourceLocation.parse(id);
        return BuiltInRegistries.ITEM.get(location);
    }

    private static ItemAttributeModifiers createSwordModifiers() {
        var attributeLookup = REGISTRY_ACCESS.lookupOrThrow(Registries.ATTRIBUTE);
        Holder<Attribute> damageAttr = attributeLookup.get(ResourceKey.create(
                Registries.ATTRIBUTE, ResourceLocation.withDefaultNamespace("generic.attack_damage"))).orElseThrow();
        Holder<Attribute> speedAttr = attributeLookup.get(ResourceKey.create(
                Registries.ATTRIBUTE, ResourceLocation.withDefaultNamespace("generic.attack_speed"))).orElseThrow();

        ItemAttributeModifiers modifiers = ItemAttributeModifiers.EMPTY;
        modifiers = modifiers.withModifierAdded(
                damageAttr,
                new AttributeModifier(ResourceLocation.withDefaultNamespace("18481079-2fa9-3c5c-aa38-fefac3636197"),
                        4.0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        modifiers = modifiers.withModifierAdded(
                speedAttr,
                new AttributeModifier(ResourceLocation.withDefaultNamespace("db990990-cabc-3dcc-8fc5-fbc9e518c6c3"),
                        -2.4000000953674316, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        return modifiers;
    }

    private static Consumer<ItemEditorContext> noop() {
        return ctx -> {};
    }

    private static class TestItemEditorModel extends ItemEditorModel {
        TestItemEditorModel(ItemEditorContext context) {
            super(context);
        }

        @Override
        protected RegistryAccess.Frozen getRegistryAccess() {
            return REGISTRY_ACCESS;
        }
    }
}
