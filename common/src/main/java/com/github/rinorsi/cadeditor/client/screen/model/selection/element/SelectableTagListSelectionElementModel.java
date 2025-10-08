package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import com.github.franckyi.databindings.api.BooleanProperty;
import net.minecraft.resources.ResourceLocation;

/**
 * Tag selection element that exposes a selection state for multi-select screens.
 */
public class SelectableTagListSelectionElementModel extends TagListSelectionElementModel implements SelectableListSelectionElementModel {
    private final BooleanProperty selectedProperty = BooleanProperty.create(false);

    public SelectableTagListSelectionElementModel(ResourceLocation id) {
        super(id);
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }
}
