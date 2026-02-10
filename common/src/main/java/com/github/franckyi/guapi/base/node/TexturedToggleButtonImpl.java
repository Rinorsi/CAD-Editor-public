package com.github.franckyi.guapi.base.node;

import com.github.franckyi.guapi.api.node.TexturedToggleButton;
import com.github.franckyi.guapi.api.node.builder.TexturedToggleButtonBuilder;
import net.minecraft.resources.Identifier;

public class TexturedToggleButtonImpl extends AbstractTexturedToggleButton implements TexturedToggleButtonBuilder {
    public TexturedToggleButtonImpl(Identifier textureId, boolean drawButton) {
        super(textureId, drawButton);
    }

    public TexturedToggleButtonImpl(Identifier textureId, int imageWidth, int imageHeight, boolean drawButton) {
        super(textureId, imageWidth, imageHeight, drawButton);
    }

    @Override
    protected Class<?> getType() {
        return TexturedToggleButton.class;
    }
}
