package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.EventTarget;
import com.github.franckyi.guapi.api.Renderable;
import com.github.franckyi.guapi.api.event.*;
import com.github.franckyi.guapi.api.node.Labeled;
import com.github.franckyi.guapi.api.node.Node;
import com.github.rinorsi.cadeditor.mixin.AbstractWidgetMixin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonInfo;

public interface VanillaWidgetSkinDelegate extends Renderable, EventTarget, GuiEventListener {
    private static net.minecraft.client.input.MouseButtonEvent toNativeMouseButtonEvent(MouseButtonEvent event) {
        return new net.minecraft.client.input.MouseButtonEvent(
                event.getMouseX(),
                event.getMouseY(),
                new MouseButtonInfo(event.getButton(), 0));
    }

    private static net.minecraft.client.input.KeyEvent toNativeKeyEvent(KeyEvent event) {
        return new net.minecraft.client.input.KeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers());
    }

    @Override
    default void mouseClicked(MouseButtonEvent event) {
        ((GuiEventListener) this).mouseClicked(toNativeMouseButtonEvent(event), false);
    }

    @Override
    default void mouseReleased(MouseButtonEvent event) {
        ((GuiEventListener) this).mouseReleased(toNativeMouseButtonEvent(event));
    }

    @Override
    default void mouseDragged(MouseDragEvent event) {
        ((GuiEventListener) this).mouseDragged(toNativeMouseButtonEvent(event), event.getDeltaX(), event.getDeltaY());
    }

    @Override
    default void mouseScrolled(MouseScrollEvent event) {
        mouseScrolled(event.getMouseX(), event.getMouseY(), event.getDeltaX(), event.getDeltaY());
    }

    @Override
    default void keyPressed(KeyEvent event) {
        ((GuiEventListener) this).keyPressed(toNativeKeyEvent(event));
    }

    @Override
    default void keyReleased(KeyEvent event) {
        ((GuiEventListener) this).keyReleased(toNativeKeyEvent(event));
    }

    @Override
    default void charTyped(TypeEvent event) {
        ((GuiEventListener) this).charTyped(new net.minecraft.client.input.CharacterEvent(event.getCharacter(), event.getModifiers()));
    }

    @Override
    default void mouseMoved(MouseEvent event) {
        mouseMoved(event.getMouseX(), event.getMouseY());
    }

    default void initNodeWidget(Node node) {
        AbstractWidget widget = (AbstractWidget) this;
        widget.active = !node.isDisabled();
        widget.visible = node.isVisible();
        node.xProperty().addListener(widget::setX);
        node.yProperty().addListener(widget::setY);
        node.widthProperty().addListener(widget::setWidth);
        node.heightProperty().addListener(((AbstractWidgetMixin) widget)::setHeight);
        node.disabledProperty().addListener(newVal -> widget.active = !newVal);
        node.visibleProperty().addListener(newVal -> widget.visible = newVal);
    }

    default void initLabeledWidget(Labeled node) {
        initNodeWidget(node);
        node.labelProperty().addListener(((AbstractWidget) this)::setMessage);
    }

    @Override
    default void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        ((AbstractWidget) this).render(guiGraphics, mouseX, mouseY, delta);
    }
}
