package com.github.franckyi.guapi.api.node.builder.generic;

import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.Scene;
import com.github.franckyi.guapi.api.node.builder.Builder;
import com.github.franckyi.guapi.api.util.Insets;

import java.util.function.Consumer;

public interface GenericSceneBuilder<S extends Scene> extends Scene, Builder<S> {
    default S root(Node value) {
        return with(s -> s.setRoot(value));
    }

    default S add(Node value) {
        return root(value);
    }

    default S fullScreen(boolean value) {
        return with(s -> s.setFullScreen(value));
    }

    default S fullScreen() {
        return fullScreen(true);
    }

    default S texturedBackground(boolean value) {
        return with(s -> s.setTexturedBackground(value));
    }

    default S texturedBackground() {
        return texturedBackground(true);
    }

    default S padding(Insets value) {
        return with(s -> s.setPadding(value));
    }

    default S closeOnEsc(boolean value) {
        return with(s -> s.setCloseOnEsc(value));
    }

    default S show(Consumer<Scene> listener) {
        return with(s -> s.onShow(listener));
    }

    default S hide(Consumer<Scene> listener) {
        return with(s -> s.onHide(listener));
    }
}
