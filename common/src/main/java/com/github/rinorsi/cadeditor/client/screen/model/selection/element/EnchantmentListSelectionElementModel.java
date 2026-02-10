package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Locale;
import java.util.function.Supplier;

public class EnchantmentListSelectionElementModel extends ItemListSelectionElementModel {
    private final Holder<Enchantment> enchantment;
    private final Component categoryLabel;

    public EnchantmentListSelectionElementModel(String name, Identifier id, Holder<Enchantment> enchantment,
                                                ItemStack item, Component categoryLabel) {
        this(name, id, enchantment, () -> item == null ? ItemStack.EMPTY : item.copy(), categoryLabel);
    }

    public EnchantmentListSelectionElementModel(String name, Identifier id, Holder<Enchantment> enchantment,
                                                Supplier<ItemStack> itemSupplier, Component categoryLabel) {
        super(name, id, itemSupplier);
        this.enchantment = enchantment;
        this.categoryLabel = categoryLabel;
    }

    public Holder<Enchantment> getEnchantment() {
        return enchantment;
    }

    public Enchantment getEnchantmentValue() {
        return enchantment.value();
    }

    public Component getCategoryLabel() {
        return categoryLabel;
    }

    @Override
    public boolean matches(String s) {
        if (super.matches(s)) {
            return true;
        }
        if (s.isEmpty()) {
            return true;
        }
        String lower = s.toLowerCase(Locale.ROOT);
        return categoryLabel.getString().toLowerCase(Locale.ROOT).contains(lower);
    }
}
