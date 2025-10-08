package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.TexturedButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
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
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (node.isDrawButton()) {
            super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        }
    }
}
