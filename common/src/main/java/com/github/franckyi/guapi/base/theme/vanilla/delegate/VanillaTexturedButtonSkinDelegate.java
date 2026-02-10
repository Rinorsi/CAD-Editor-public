package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.TexturedButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@SuppressWarnings("this-escape")
public class VanillaTexturedButtonSkinDelegate<N extends TexturedButton> extends Button implements VanillaWidgetSkinDelegate {
    protected final N node;

    public VanillaTexturedButtonSkinDelegate(N node) {
        super(node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.getTooltip().isEmpty() ? Component.empty() : node.getTooltip().get(0), button -> {
        }, Supplier::get);
        this.node = node;
        initNodeWidget(node);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (node.isDrawButton()) {
            super.render(guiGraphics, mouseX, mouseY, delta);
        } else {
            this.isHovered = this.active && this.visible && this.isMouseOver(mouseX, mouseY);
        }
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (node.isDrawButton()) {
            renderDefaultSprite(guiGraphics);
        }
        if (getMessage().getString().isEmpty()) {
            return;
        }
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
