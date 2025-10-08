package com.github.franckyi.guapi.base.theme.vanilla;

import com.github.franckyi.guapi.api.RenderHelper;
import com.github.franckyi.guapi.api.node.EnumButton;
import com.github.franckyi.guapi.base.theme.vanilla.delegate.VanillaButtonSkinDelegate;
import com.github.franckyi.guapi.base.theme.vanilla.delegate.VanillaEnumButtonSkinDelegate;
import net.minecraft.network.chat.Component;

@SuppressWarnings("unused")
public class VanillaEnumButtonSkin extends VanillaButtonSkin<EnumButton<?>> {
    public VanillaEnumButtonSkin(EnumButton<?> node) {
        this(node, new VanillaEnumButtonSkinDelegate(node));
    }

    protected VanillaEnumButtonSkin(EnumButton<?> node, VanillaButtonSkinDelegate<EnumButton<?>> delegate) {
        super(node, delegate);
    }

    @Override
    public int computeWidth(EnumButton<?> node) {
        return computeWidthInternal(node);
    }

    private static <E> int computeWidthInternal(EnumButton<E> node) {
        return node.getValues().stream()
                .mapToInt(value -> RenderHelper.getFontWidth(node.getTextFactory().apply(value)))
                .max().orElse(0) + 20;
    }
}
