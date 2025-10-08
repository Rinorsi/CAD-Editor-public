package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import com.github.franckyi.databindings.api.BooleanProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SortedEnchantmentListSelectionElementModel extends EnchantmentListSelectionElementModel implements SelectableListSelectionElementModel {
    private final boolean curse;
    private final boolean canApply;
    private final BooleanProperty selectedProperty = BooleanProperty.create(false);

    public SortedEnchantmentListSelectionElementModel(EnchantmentListSelectionElementModel item, boolean curse, boolean canApply) {
        super(item.getName(), item.getId(), item.getEnchantment(), item.getItem(), item.getCategoryLabel());
        this.curse = curse;
        this.canApply = canApply;
    }

    public boolean isCurse() {
        return curse;
    }

    public boolean canApply() {
        return canApply;
    }

    @Override
    public Type getType() {
        return Type.ENCHANTMENT;
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }

    @Override
    public Component getDisplayName() {
        ChatFormatting color = isCurse() ? ChatFormatting.RED : (canApply() ? ChatFormatting.GREEN : ChatFormatting.GRAY);
        MutableComponent displayName = Component.literal(getName());
        return displayName.withStyle(style -> style.withBold(true).withColor(color));
    }
}
