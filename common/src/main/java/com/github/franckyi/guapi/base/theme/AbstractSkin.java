package com.github.franckyi.guapi.base.theme;

import com.github.franckyi.guapi.api.Guapi;
import com.github.franckyi.guapi.api.RenderHelper;
import com.github.franckyi.guapi.api.event.ScreenEvent;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.theme.Skin;
import com.github.franckyi.guapi.api.util.DebugMode;
import com.github.franckyi.guapi.api.util.ScreenEventType;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Random;

public abstract class AbstractSkin<N extends Node> implements Skin<N> {
    private final int debugColor;

    protected AbstractSkin() {
        debugColor = new Random().nextInt(0x1000000) + 0x80000000;
    }

    @Override
    public boolean preRender(N node, GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        return false;
    }

    @Override
    public void render(N node, GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(node, guiGraphics);
    }

    @Override
    public void postRender(N node, GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        DebugMode mode = Guapi.getDebugMode();
        if (mode == DebugMode.UI) {
            DebugLog.ui(nodeDebugKey(node), () -> describeNode(node, mouseX, mouseY));
            renderDebug(node, guiGraphics);
        }
        if (!node.getTooltip().isEmpty() && node.isHovered()) {
            RenderHelper.drawTooltip(guiGraphics, node.getTooltip(), mouseX, mouseY);
        }
    }

    @Override
    public <E extends ScreenEvent> void onEvent(ScreenEventType<E> type, E event) {
        type.onEvent(this, event);
    }

    protected void renderDebug(N node, GuiGraphics guiGraphics) {
        RenderHelper.drawRectangle(guiGraphics, node.getLeft(), node.getTop(),
                node.getRight(), node.getBottom(), debugColor);
    }

    protected void renderBackground(N node, GuiGraphics guiGraphics) {
        RenderHelper.fillRectangle(guiGraphics, node.getLeft(), node.getTop(),
                node.getRight(), node.getBottom(), node.getBackgroundColor());
    }

    private String nodeDebugKey(N node) {
        return node.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(node));
    }

    private String describeNode(N node, int mouseX, int mouseY) {
        String parent = node.getParent() != null ? node.getParent().getClass().getSimpleName() : "<root>";
        return String.format(
                "node=%s bounds=[%d,%d -> %d,%d] size[w=%d,h=%d pref=%dx%d computed=%dx%d] hovered=%s visible=%s parent=%s mouse=(%d,%d)",
                node.getClass().getSimpleName(),
                node.getLeft(), node.getTop(), node.getRight(), node.getBottom(),
                node.getWidth(), node.getHeight(),
                node.getPrefWidth(), node.getPrefHeight(),
                node.getComputedWidth(), node.getComputedHeight(),
                node.isHovered(), node.isVisible(), parent,
                mouseX, mouseY);
    }
}
