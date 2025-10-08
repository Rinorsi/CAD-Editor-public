package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.franckyi.databindings.api.ObservableBooleanValue;
import com.github.franckyi.guapi.api.Guapi;
import com.github.franckyi.guapi.api.mvc.Model;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.context.EditorContext;
import com.github.rinorsi.cadeditor.common.EditorType;

public interface EditorModel extends Model {
    EditorContext<?> getContext();

    default boolean isValid() {
        return validProperty().getValue();
    }

    ObservableBooleanValue validProperty();

    default void changeEditor(EditorType type) {
        apply();
        ModScreenHandler.openEditor(type, getContext(), true);
    }

    void apply();

    default void save() {
        apply();
        getContext().update();
    }

    default void update() {
        apply();
        getContext().update();
        Guapi.getScreenHandler().hideScene();
    }
}
