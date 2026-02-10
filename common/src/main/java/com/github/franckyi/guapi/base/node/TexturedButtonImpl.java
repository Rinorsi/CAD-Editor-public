package com.github.franckyi.guapi.base.node;

import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.franckyi.guapi.api.node.builder.TexturedButtonBuilder;
import net.minecraft.resources.Identifier;

public class TexturedButtonImpl extends AbstractTexturedButton implements TexturedButtonBuilder {
    public TexturedButtonImpl(Identifier textureId, boolean drawButton) {
        super(textureId, drawButton);
    }

    public TexturedButtonImpl(Identifier textureId, int imageWidth, int imageHeight, boolean drawButton) {
        super(textureId, imageWidth, imageHeight, drawButton);
    }

    @Override
    protected Class<?> getType() {
        return TexturedButton.class;
    }

    @Override
    public String toString() {
        return "TexturedButton{\"" + getTextureId() + "\"}";
    }
}
