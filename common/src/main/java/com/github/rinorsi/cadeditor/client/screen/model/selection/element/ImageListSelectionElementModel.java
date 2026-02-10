package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.resources.Identifier;

public class ImageListSelectionElementModel extends ListSelectionElementModel {
    private final Identifier textureId;

    public ImageListSelectionElementModel(String name, Identifier id, Identifier textureId) {
        super(name, id);
        this.textureId = textureId;
    }

    public Identifier getTextureId() {
        return textureId;
    }

    @Override
    public Type getType() {
        return Type.IMAGE;
    }
}
