package com.github.rinorsi.cadeditor.client.theme;

import com.github.franckyi.guapi.api.node.Button;
import com.github.franckyi.guapi.base.theme.vanilla.delegate.VanillaButtonSkinDelegate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class MonochromeButtonSkinDelegate<N extends Button> extends VanillaButtonSkinDelegate<N> {
    private static final int NORMAL_BG = 0xFF2A2A2E;
    private static final int HOVER_BG = 0xFF3A3A40;
    private static final int DISABLED_BG = 0xFF1C1C1F;
    private static final int BORDER_COLOR = 0xFF55555A;
    private static final int FOCUS_BORDER_COLOR = 0xFF7A7A80;
    private static final int NORMAL_TEXT = 0xFFEEEEEE;
    private static final int DISABLED_TEXT = 0xFF77777C;

    public MonochromeButtonSkinDelegate(N node) {
        super(node);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        int backgroundColor = resolveBackgroundColor();
        guiGraphics.fill(x, y, x + width, y + height, backgroundColor);
        drawBorders(guiGraphics, x, y, width, height);
        drawLabel(guiGraphics, x, y, width, height);
    }

    private int resolveBackgroundColor() {
        if (!active) {
            return DISABLED_BG;
        }
        return isHoveredOrFocused() ? HOVER_BG : NORMAL_BG;
    }

    private void drawBorders(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int topColor = isFocused() ? FOCUS_BORDER_COLOR : BORDER_COLOR;
        int bottomColor = isFocused() ? FOCUS_BORDER_COLOR : BORDER_COLOR;
        guiGraphics.fill(x, y, x + width, y + 1, topColor);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, bottomColor);
        guiGraphics.fill(x, y, x + 1, y + height, topColor);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, bottomColor);
    }

    private void drawLabel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        Font font = Minecraft.getInstance().font;
        int textColor = active ? NORMAL_TEXT : DISABLED_TEXT;
        int textY = y + (height - 8) / 2;
        guiGraphics.drawCenteredString(font, getMessage(), x + width / 2, textY, textColor);
    }
}
