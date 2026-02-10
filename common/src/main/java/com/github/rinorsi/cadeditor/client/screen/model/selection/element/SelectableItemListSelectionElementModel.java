package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import com.github.franckyi.databindings.api.BooleanProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * A {@link ItemListSelectionElementModel} variant that supports multi-selection.
 */
public class SelectableItemListSelectionElementModel extends ItemListSelectionElementModel implements SelectableListSelectionElementModel {
    private final BooleanProperty selectedProperty = BooleanProperty.create(false);

    public SelectableItemListSelectionElementModel(String name, Identifier id, ItemStack itemStack) {
        super(name, id, itemStack);
    }

    public SelectableItemListSelectionElementModel(String name, Identifier id, Supplier<ItemStack> itemSupplier) {
        super(name, id, itemSupplier);
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }
}
