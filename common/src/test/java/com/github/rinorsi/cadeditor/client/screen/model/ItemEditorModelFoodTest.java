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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        Assertions.assertTrue(
                immediateTag.getCompound("components")
                        .map(tag -> tag.contains("minecraft:food"))
                        .orElse(false),
                "Food component missing immediately after enabling"
        );

        model.apply();

        ItemStack updated = context.getItemStack();
        var foodComponent = Objects.requireNonNull(
                updated.get(DataComponents.FOOD),
                "Expected food component on context stack"
        );
        var consumableComponent = Objects.requireNonNull(
                updated.get(DataComponents.CONSUMABLE),
                "Expected consumable component on context stack"
        );
        Assertions.assertEquals(1.6f, consumableComponent.consumeSeconds(), 1e-6f);

        CompoundTag tag = context.getTag();
        Assertions.assertNotNull(tag, "Context tag should be initialized");
        CompoundTag components = requireCompound(tag, "components");
        Assertions.assertTrue(components.contains("minecraft:food"), "Food component missing in tag");
        CompoundTag food = requireCompound(components, "minecraft:food");
        Assertions.assertEquals(0, food.getInt("nutrition").orElse(0));
        Assertions.assertEquals(0.0f, food.getFloat("saturation").orElse(0.0f));
        Assertions.assertTrue(food.getBoolean("can_always_eat").orElse(false));
        food.getFloat("eat_seconds").ifPresent(value -> Assertions.assertEquals(1.6f, value, 1e-6f));
        if (food.contains("using_converts_to")) {
            Assertions.fail("using_converts_to should not be present");
        }
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
        CompoundTag enabledComponents = requireCompound(enabledTag, "components");
        Assertions.assertTrue(enabledComponents.contains("minecraft:food"), "Food component missing after enabling");
        Assertions.assertFalse(enabledComponents.contains("!minecraft:food"), "Removal marker should not persist while enabled");

        model.disableFoodComponent();

        CompoundTag disabledTag = context.getTag();
        Assertions.assertNotNull(disabledTag, "Tag should remain available after disabling");
        CompoundTag disabledComponents = requireCompound(disabledTag, "components");
        Assertions.assertFalse(disabledComponents.contains("minecraft:food"), "Food component should be cleared when disabled");
        Assertions.assertTrue(disabledComponents.contains("!minecraft:food"), "Removal marker missing when food disabled");

        ItemStack disabledStack = context.getItemStack();
        Assertions.assertFalse(disabledStack.has(DataComponents.FOOD), () -> "Stack should stop being edible after disabling. Tag=" + disabledComponents);

        model.enableFoodComponent();

        ItemStack reenabledStack = context.getItemStack();
        Assertions.assertTrue(reenabledStack.has(DataComponents.FOOD), "Stack should be edible again after re-enabling");

        CompoundTag reenabledTag = context.getTag();
        Assertions.assertNotNull(reenabledTag, "Tag should still be initialized after re-enabling");
        CompoundTag reenabledComponents = requireCompound(reenabledTag, "components");
        Assertions.assertTrue(reenabledComponents.contains("minecraft:food"), "Food component missing after re-enabling");
        Assertions.assertFalse(reenabledComponents.contains("!minecraft:food"), "Removal marker should be cleared after re-enabling");
    }

    @Test
    void switchingConvertTargetPreservesComponents() {
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
        CompoundTag components = requireCompound(tag, "components");
        Assertions.assertFalse(components.contains("!minecraft:lore"));
        Assertions.assertFalse(components.contains("!minecraft:rarity"));
        Assertions.assertFalse(components.contains("!minecraft:repair_cost"));
        var remainder = updated.get(DataComponents.USE_REMAINDER);
        Assertions.assertNotNull(remainder, "Expected use remainder component");
        ItemStack remainderStack = remainder.convertInto().copy();
        Assertions.assertEquals(getItem("minecraft:diamond_sword"), remainderStack.getItem());
    }

    @Test
    void customConvertTargetRetainsEditedComponents() {
        ItemStack baseStack = new ItemStack(getItem("minecraft:apple"));
        ItemEditorContext context = new ItemEditorContext(baseStack, null, true, noop());
        TestItemEditorModel model = new TestItemEditorModel(context);

        model.initalize();
        model.enableFoodComponent();

        ItemStack convertStack = new ItemStack(getItem("minecraft:diamond_sword"));
        convertStack.set(DataComponents.CUSTOM_NAME, Component.literal("Blade"));
        convertStack.set(DataComponents.ATTRIBUTE_MODIFIERS, createSwordModifiers());

        model.getFoodState().useCustomUsingConvertsTo(convertStack);
        model.apply();

        CompoundTag tag = context.getTag();
        Assertions.assertNotNull(tag, "Context tag should be initialized");
        ItemStack updated = context.getItemStack();
        var remainder = updated.get(DataComponents.USE_REMAINDER);
        Assertions.assertNotNull(remainder, "Expected use remainder component");
        ItemStack remainderStack = remainder.convertInto().copy();
        Assertions.assertEquals(getItem("minecraft:diamond_sword"), remainderStack.getItem());
        Assertions.assertNotNull(remainderStack.get(DataComponents.ATTRIBUTE_MODIFIERS));
        Assertions.assertNotNull(remainderStack.get(DataComponents.CUSTOM_NAME));
    }

    private static Item getItem(String id) {
        Identifier location = Identifier.parse(id);
        return BuiltInRegistries.ITEM.getOptional(location)
                .orElseThrow(() -> new IllegalArgumentException("Unknown item id: " + id));
    }

    private static CompoundTag requireCompound(CompoundTag tag, String key) {
        return tag.getCompound(key)
                .orElseThrow(() -> new AssertionError("Missing compound tag: " + key));
    }

    private static ItemAttributeModifiers createSwordModifiers() {
        Holder<Attribute> damageAttr = Attributes.ATTACK_DAMAGE;
        Holder<Attribute> speedAttr = Attributes.ATTACK_SPEED;

        ItemAttributeModifiers modifiers = ItemAttributeModifiers.EMPTY;
        modifiers = modifiers.withModifierAdded(
                damageAttr,
                new AttributeModifier(Identifier.withDefaultNamespace("18481079-2fa9-3c5c-aa38-fefac3636197"),
                        4.0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        modifiers = modifiers.withModifierAdded(
                speedAttr,
                new AttributeModifier(Identifier.withDefaultNamespace("db990990-cabc-3dcc-8fc5-fbc9e518c6c3"),
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
