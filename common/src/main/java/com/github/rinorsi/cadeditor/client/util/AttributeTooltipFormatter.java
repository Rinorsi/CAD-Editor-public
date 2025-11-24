package com.github.rinorsi.cadeditor.client.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Ensures tooltip attribute lines always follow vanilla's “final value” presentation,
 * even when the vanilla section is hidden or replaced.
 */
public final class AttributeTooltipFormatter {
    private AttributeTooltipFormatter() {
    }

    public static List<Component> buildTooltipLines(ItemStack stack, Item.TooltipContext context,
                                                    @Nullable Player player, TooltipFlag flag) {
        List<Component> base = stack.getTooltipLines(context, player, flag);
        FilterResult filtered = filterVanillaAttributeSections(base);
        List<Component> attributeLines = buildAttributeLines(stack, player);
        if (!attributeLines.isEmpty()) {
            filtered.lines.addAll(attributeLines);
        }
        return filtered.lines;
    }

    private static List<Component> buildAttributeLines(ItemStack stack, @Nullable Player player) {
        List<Component> result = new ArrayList<>();
        for (EquipmentSlotGroup slot : EquipmentSlotGroup.values()) {
            final boolean[] headerAdded = {false};
            stack.forEachModifier(slot, (attribute, modifier, display) -> {
                if (!headerAdded[0]) {
                    headerAdded[0] = true;
                    result.add(CommonComponents.EMPTY);
                    result.add(Component.translatable("item.modifiers." + slot.getSerializedName())
                            .withStyle(ChatFormatting.GRAY));
                }
                Component line = formatModifier(attribute, modifier, player);
                if (line != null) {
                    result.add(line);
                }
            });
        }
        return result;
    }

    private static Component formatModifier(Holder<Attribute> attribute, AttributeModifier modifier,
                                            @Nullable Player player) {
        double amount = modifier.amount();
        boolean includeBase = shouldIncludeBase(modifier);
        if (includeBase) {
            amount += getBaseValueFor(modifier, player);
        }
        double displayAmount = amount;
        if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                || modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            displayAmount = amount * 100.0;
        } else if (attribute.value() == Attributes.KNOCKBACK_RESISTANCE.value()) {
            displayAmount = amount * 10.0;
        }
        MutableComponent attributeName = Component.translatable(attribute.value().getDescriptionId());
        if (includeBase) {
            return CommonComponents.space()
                    .append(Component.translatable(
                            "attribute.modifier.equals." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmount),
                            attributeName))
                    .withStyle(ChatFormatting.DARK_GREEN);
        }
        if (amount > 0.0) {
            return Component.translatable(
                    "attribute.modifier.plus." + modifier.operation().id(),
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmount),
                    attributeName
            ).withStyle(attribute.value().getStyle(true));
        }
        if (amount < 0.0) {
            return Component.translatable(
                    "attribute.modifier.take." + modifier.operation().id(),
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-displayAmount),
                    attributeName
            ).withStyle(attribute.value().getStyle(false));
        }
        return null;
    }

    private static boolean shouldIncludeBase(AttributeModifier modifier) {
        return modifier.is(Item.BASE_ATTACK_DAMAGE_ID) || modifier.is(Item.BASE_ATTACK_SPEED_ID);
    }

    private static double getBaseValueFor(AttributeModifier modifier, @Nullable Player player) {
        if (modifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
            if (player != null) {
                return player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
            }
            return Attributes.ATTACK_DAMAGE.value().getDefaultValue();
        }
        if (modifier.is(Item.BASE_ATTACK_SPEED_ID)) {
            if (player != null) {
                return player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
            }
            return Attributes.ATTACK_SPEED.value().getDefaultValue();
        }
        return 0.0;
    }

    private static FilterResult filterVanillaAttributeSections(List<Component> base) {
        List<Component> filtered = new ArrayList<>(base.size());
        boolean removed = false;
        for (int i = 0; i < base.size(); i++) {
            Component line = base.get(i);
            if (isSpacer(line) && i + 1 < base.size() && isModifierHeader(base.get(i + 1))) {
                removed = true;
                i++;
                while (i + 1 < base.size() && isAttributeLine(base.get(i + 1))) {
                    i++;
                }
                continue;
            }
            if (isModifierHeader(line) || isAttributeLine(line)) {
                removed = true;
                continue;
            }
            filtered.add(line);
        }
        return new FilterResult(filtered, removed);
    }

    private static boolean isSpacer(Component component) {
        return component == CommonComponents.EMPTY || component.getString().isEmpty();
    }

    private static boolean isModifierHeader(Component component) {
        return component.getContents() instanceof TranslatableContents contents
                && contents.getKey().startsWith("item.modifiers.");
    }

    private static boolean isAttributeLine(Component component) {
        return component.getContents() instanceof TranslatableContents contents
                && contents.getKey().startsWith("attribute.modifier.");
    }

    private record FilterResult(List<Component> lines, boolean removedAny) {
    }
}
