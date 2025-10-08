package com.github.franckyi.guapi.api.node.builder.generic;

import com.github.franckyi.guapi.api.event.*;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.builder.Builder;
import com.github.franckyi.guapi.api.util.Insets;
import com.github.franckyi.guapi.api.util.ScreenEventType;
import net.minecraft.network.chat.Component;

public interface GenericNodeBuilder<N extends Node> extends Node, Builder<N> {
    default N minWidth(int value) {
        return with(n -> n.setMinWidth(value));
    }

    default N minHeight(int value) {
        return with(n -> n.setMinHeight(value));
    }

    default N minSize(int width, int height) {
        minWidth(width);
        return minHeight(height);
    }

    default N prefWidth(int value) {
        return with(n -> n.setPrefWidth(value));
    }

    default N prefHeight(int value) {
        return with(n -> n.setPrefHeight(value));
    }

    default N prefSize(int width, int height) {
        prefWidth(width);
        return prefHeight(height);
    }

    default N maxWidth(int value) {
        return with(n -> n.setMaxWidth(value));
    }

    default N maxHeight(int value) {
        return with(n -> n.setMaxHeight(value));
    }

    default N maxSize(int width, int height) {
        maxWidth(width);
        return maxHeight(height);
    }

    default N backgroundColor(int value) {
        return with(n -> n.setBackgroundColor(value));
    }

    default N padding(Insets value) {
        return with(n -> n.setPadding(value));
    }

    default N padding(int value) {
        return padding(new Insets(value));
    }

    default N padding(int topBottom, int rightLeft) {
        return padding(new Insets(topBottom, rightLeft));
    }

    default N padding(int top, int rightLeft, int bottom) {
        return padding(new Insets(top, rightLeft, bottom));
    }

    default N padding(int top, int right, int bottom, int left) {
        return padding(new Insets(top, right, bottom, left));
    }

    default N tooltip(Component... value) {
        return with(n -> n.getTooltip().setAll(value));
    }

    default N visible(boolean value) {
        return with(n -> n.setVisible(value));
    }

    default N invisible() {
        return visible(false);
    }

    default N disable(boolean value) {
        return with(n -> n.setDisable(value));
    }

    default N disable() {
        return disable(true);
    }

    default <E extends ScreenEvent> N listener(ScreenEventType<E> target, ScreenEventListener<E> listener) {
        return with(n -> n.addListener(target, listener));
    }

    default <E extends ScreenEvent> N listener(ScreenEventType<E> target, Runnable listener) {
        return with(n -> n.addListener(target, listener));
    }

    default N mouseClicked(ScreenEventListener<MouseButtonEvent> listener) {
        return with(n -> n.onMouseClick(listener));
    }

    default N mouseReleased(ScreenEventListener<MouseButtonEvent> listener) {
        return with(n -> n.onMouseRelease(listener));
    }

    default N mouseDragged(ScreenEventListener<MouseDragEvent> listener) {
        return with(n -> n.onMouseDrag(listener));
    }

    default N mouseScrolled(ScreenEventListener<MouseScrollEvent> listener) {
        return with(n -> n.onMouseScroll(listener));
    }

    default N keyPressed(ScreenEventListener<KeyEvent> listener) {
        return with(n -> n.onKeyPress(listener));
    }

    default N keyReleased(ScreenEventListener<KeyEvent> listener) {
        return with(n -> n.onKeyRelease(listener));
    }

    default N charTyped(ScreenEventListener<TypeEvent> listener) {
        return with(n -> n.onCharType(listener));
    }

    default N mouseMoved(ScreenEventListener<MouseEvent> listener) {
        return with(n -> n.onMouseMove(listener));
    }

    default N action(ScreenEventListener<MouseButtonEvent> listener) {
        return with(n -> n.onAction(listener));
    }

    default N mouseClicked(Runnable listener) {
        return with(n -> n.onMouseClick(listener));
    }

    default N mouseReleased(Runnable listener) {
        return with(n -> n.onMouseRelease(listener));
    }

    default N mouseDragged(Runnable listener) {
        return with(n -> n.onMouseDrag(listener));
    }

    default N mouseScrolled(Runnable listener) {
        return with(n -> n.onMouseScroll(listener));
    }

    default N keyPressed(Runnable listener) {
        return with(n -> n.onKeyPress(listener));
    }

    default N keyReleased(Runnable listener) {
        return with(n -> n.onKeyRelease(listener));
    }

    default N charTyped(Runnable listener) {
        return with(n -> n.onCharType(listener));
    }

    default N mouseMoved(Runnable listener) {
        return with(n -> n.onMouseMove(listener));
    }

    default N action(Runnable listener) {
        return with(n -> n.onAction(listener));
    }
}
