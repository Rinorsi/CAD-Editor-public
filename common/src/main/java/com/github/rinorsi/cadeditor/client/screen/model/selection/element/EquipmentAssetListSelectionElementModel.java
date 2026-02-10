package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/**
 * Simple list element that displays equipment asset identifiers using literal text instead of translation keys.
 */
public class EquipmentAssetListSelectionElementModel extends ListSelectionElementModel {

    private final Component displayName;
    private final String lowerCaseId;

    public EquipmentAssetListSelectionElementModel(Identifier id) {
        super(id.toString(), id);
        this.displayName = Component.literal(id.toString());
        this.lowerCaseId = id.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public boolean matches(String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }
        return lowerCaseId.contains(query.toLowerCase(Locale.ROOT));
    }
}
