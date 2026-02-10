package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Selection element that renders loot table identifiers as plain text.
 */
public class LootTableListSelectionElementModel extends ListSelectionElementModel {
    public LootTableListSelectionElementModel(Identifier id) {
        super(id.toString(), id);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(getId().toString());
    }
}
