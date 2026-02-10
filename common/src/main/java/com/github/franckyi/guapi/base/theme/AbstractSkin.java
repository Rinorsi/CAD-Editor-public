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
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public abstract class AbstractSkin<N extends Node> implements Skin<N> {
    private final int debugColor;
    private static final Map<Node, Integer> NODE_IDS = new ConcurrentHashMap<>();
    private static final AtomicInteger NODE_ID_SEQ = new AtomicInteger();

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
            RenderHelper.drawTooltip(guiGraphics, node.getTooltip(),
                    getTooltipX(node, mouseX, mouseY),
                    getTooltipY(node, mouseX, mouseY));
        }
    }

    protected int getTooltipX(N node, int mouseX, int mouseY) {
        return mouseX;
    }

    protected int getTooltipY(N node, int mouseX, int mouseY) {
        return mouseY;
    }

    @Override
    public <E extends ScreenEvent> void onEvent(ScreenEventType<E> type, E event) {
        type.onEvent(this, event);
    }

    protected void renderDebug(N node, GuiGraphics guiGraphics) {
        RenderHelper.drawRectangle(guiGraphics, node.getLeft(), node.getTop(),
                node.getRight(), node.getBottom(), debugColor);
        if (node.getHeight() > 20) {
            int id = nodeId(node);
            String label = "#" + id + " " + node.getClass().getSimpleName();
            RenderHelper.drawString(guiGraphics, Component.literal(label),
                    node.getLeft() + 2, node.getTop() + 2, 0xFFFFFFFF, true);
        }
    }

    protected void renderBackground(N node, GuiGraphics guiGraphics) {
        RenderHelper.fillRectangle(guiGraphics, node.getLeft(), node.getTop(),
                node.getRight(), node.getBottom(), node.getBackgroundColor());
    }

    private String nodeDebugKey(N node) {
        return node.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(node));
    }

    private String describeNode(N node, int mouseX, int mouseY) {
        int id = nodeId(node);
        String parent = node.getParent() != null ? node.getParent().getClass().getSimpleName() : "<root>";
        String scene = node.getScene() != null ? node.getScene().getClass().getSimpleName() : "<none>";
        String type = node.getClass().getName();
        return String.format(
                "id=%d type=%s parent=%s scene=%s bounds=[%d,%d -> %d,%d] size[w=%d,h=%d pref=%dx%d computed=%dx%d] hovered=%s visible=%s mouse=(%d,%d)",
                id,
                type,
                parent,
                scene,
                node.getLeft(), node.getTop(), node.getRight(), node.getBottom(),
                node.getWidth(), node.getHeight(),
                node.getPrefWidth(), node.getPrefHeight(),
                node.getComputedWidth(), node.getComputedHeight(),
                node.isHovered(), node.isVisible(),
                mouseX, mouseY);
    }

    private int nodeId(Node node) {
        return NODE_IDS.computeIfAbsent(node, n -> NODE_ID_SEQ.incrementAndGet());
    }
}
