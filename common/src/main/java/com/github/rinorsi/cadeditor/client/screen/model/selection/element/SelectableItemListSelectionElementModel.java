package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import com.github.franckyi.databindings.api.BooleanProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * A {@link ItemListSelectionElementModel} variant that supports multi-selection.
 */
public class SelectableItemListSelectionElementModel extends ItemListSelectionElementModel implements SelectableListSelectionElementModel {
    private final BooleanProperty selectedProperty = BooleanProperty.create(false);

    public SelectableItemListSelectionElementModel(String name, ResourceLocation id, ItemStack itemStack) {
        super(name, id, itemStack);
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }
}
