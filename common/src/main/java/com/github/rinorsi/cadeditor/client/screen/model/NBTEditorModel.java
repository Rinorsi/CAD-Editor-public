package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.ObservableBooleanValue;
import com.github.rinorsi.cadeditor.client.context.EditorContext;
import net.minecraft.nbt.CompoundTag;

@SuppressWarnings("this-escape")
public class NBTEditorModel implements EditorModel {
    private final EditorContext<?> context;
    private final ObjectProperty<NBTTagModel> rootTagProperty;
    private final ObjectProperty<NBTTagModel> clipboardTagProperty = ObjectProperty.create();
    private final ObservableBooleanValue validProperty;

    public NBTEditorModel(EditorContext<?> context) {
        this.context = context;
        rootTagProperty = ObjectProperty.create(new NBTTagModel(getContext(), getContext().getTag()));
        validProperty = rootTagProperty().mapToObservableBoolean(NBTTagModel::validProperty);
    }

    public NBTTagModel getRootTag() {
        return rootTagProperty().getValue();
    }

    public ObjectProperty<NBTTagModel> rootTagProperty() {
        return rootTagProperty;
    }

    public void setRootTag(NBTTagModel value) {
        rootTagProperty().setValue(value);
    }

    public NBTTagModel getClipboardTag() {
        return clipboardTagProperty().getValue();
    }

    public ObjectProperty<NBTTagModel> clipboardTagProperty() {
        return clipboardTagProperty;
    }

    public void setClipboardTag(NBTTagModel value) {
        clipboardTagProperty().setValue(value);
    }

    @Override
    public void apply() {
        getContext().setTag((CompoundTag) getRootTag().build());
    }

    @Override
    public EditorContext<?> getContext() {
        return context;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }
}
