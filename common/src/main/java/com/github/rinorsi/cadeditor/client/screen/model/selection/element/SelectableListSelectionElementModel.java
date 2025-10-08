package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import com.github.franckyi.databindings.api.BooleanProperty;

public interface SelectableListSelectionElementModel {
    BooleanProperty selectedProperty();

    default boolean isSelected() {
        return selectedProperty().getValue();
    }

    default void setSelected(boolean value) {
        selectedProperty().setValue(value);
    }
}
