package com.github.rinorsi.cadeditor.client.theme;

import com.github.franckyi.guapi.api.RenderHelper;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.theme.Skin;
import com.github.franckyi.guapi.api.util.Align;
import com.github.franckyi.guapi.base.theme.AbstractSkin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public final class MonochromeLabelSkin extends AbstractSkin<Label> {
    public static final Skin<Label> INSTANCE = new MonochromeLabelSkin();
    private static final int DEFAULT_TEXT_COLOR = 0xE0E0E0;

    private MonochromeLabelSkin() {
    }

    @Override
    public void render(Label node, GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(node, guiGraphics, mouseX, mouseY, delta);
        renderText(node, guiGraphics);
    }

    private void renderText(Label node, GuiGraphics guiGraphics) {
        Component text = node.getLabel();
        int x = Align.getAlignedX(node.getTextAlign().getHorizontalAlign(), node, RenderHelper.getFontWidth(text));
        int y = Align.getAlignedY(node.getTextAlign().getVerticalAlign(), node, RenderHelper.getFontHeight());
        RenderHelper.drawString(guiGraphics, text, x, y, resolveBaseColor(text), node.hasShadow());
    }

    private static int resolveBaseColor(Component component) {
        TextColor explicitColor = findFirstColor(component);
        return explicitColor != null ? explicitColor.getValue() : DEFAULT_TEXT_COLOR;
    }

    private static TextColor findFirstColor(Component component) {
        if (component.getStyle().getColor() != null) {
            return component.getStyle().getColor();
        }
        for (Component sibling : component.getSiblings()) {
            TextColor color = findFirstColor(sibling);
            if (color != null) {
                return color;
            }
        }
        return null;
    }

    @Override
    public int computeWidth(Label node) {
        return RenderHelper.getFontWidth(node.getLabel());
    }

    @Override
    public int computeHeight(Label node) {
        return RenderHelper.getFontHeight() - 1;
    }
}
