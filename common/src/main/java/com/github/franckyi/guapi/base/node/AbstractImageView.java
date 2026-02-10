package com.github.franckyi.guapi.base.node;

import com.github.franckyi.databindings.api.IntegerProperty;
import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.guapi.api.node.ImageView;
import net.minecraft.resources.Identifier;

@SuppressWarnings("this-escape")
public abstract class AbstractImageView extends AbstractControl implements ImageView {
    private final ObjectProperty<Identifier> textureIdProperty = ObjectProperty.create();
    private final IntegerProperty imageXProperty = IntegerProperty.create();
    private final IntegerProperty imageYProperty = IntegerProperty.create();
    private final IntegerProperty imageWidthProperty = IntegerProperty.create();
    private final IntegerProperty imageHeightProperty = IntegerProperty.create();

    public AbstractImageView(Identifier textureId) {
        this(textureId, 0, 0);
    }

    public AbstractImageView(Identifier textureId, int imageWidth, int imageHeight) {
        setTextureId(textureId);
        setImageWidth(imageWidth);
        setImageHeight(imageHeight);
        setPrefWidth(imageWidth);
        setPrefHeight(imageHeight);
    }

    @Override
    public ObjectProperty<Identifier> textureIdProperty() {
        return textureIdProperty;
    }

    @Override
    public IntegerProperty imageXProperty() {
        return imageXProperty;
    }

    @Override
    public IntegerProperty imageYProperty() {
        return imageYProperty;
    }

    @Override
    public IntegerProperty imageWidthProperty() {
        return imageWidthProperty;
    }

    @Override
    public IntegerProperty imageHeightProperty() {
        return imageHeightProperty;
    }
}
