package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.Button;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

@SuppressWarnings("this-escape")
public class VanillaButtonSkinDelegate<N extends Button> extends net.minecraft.client.gui.components.Button implements VanillaWidgetSkinDelegate {
    protected final N node;

    public VanillaButtonSkinDelegate(N node) {
        super(node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.getLabel(), button -> {
        }, Supplier::get);
        this.node = node;
        initLabeledWidget(node);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderDefaultSprite(guiGraphics);
        int color = active ? 0xFFFFFF : 0xA0A0A0;
        int alphaColor = Mth.ceil(alpha * 255.0F) << 24;
        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                getMessage(),
                getX() + width / 2,
                getY() + (height - 8) / 2,
                color | alphaColor
        );
    }
}
