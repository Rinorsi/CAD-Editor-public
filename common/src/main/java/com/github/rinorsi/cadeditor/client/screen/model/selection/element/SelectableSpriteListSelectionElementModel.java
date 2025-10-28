package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import com.github.franckyi.databindings.api.BooleanProperty;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Sprite list element that exposes a selectable state so list selection screens can
 * render a checkbox (used for multi-selection of mob effects).
 */
public class SelectableSpriteListSelectionElementModel extends SpriteListSelectionElementModel implements SelectableListSelectionElementModel {
    private final BooleanProperty selectedProperty = BooleanProperty.create(false);

    public SelectableSpriteListSelectionElementModel(String name, ResourceLocation id, Supplier<TextureAtlasSprite> spriteFactory) {
        super(name, id, spriteFactory);
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }
}
