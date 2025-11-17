package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.franckyi.databindings.api.ObservableBooleanValue;
import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.StringProperty;
import com.github.rinorsi.cadeditor.client.context.EditorContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.github.rinorsi.cadeditor.client.util.SnbtHelper;

@SuppressWarnings("this-escape")
public class SNBTEditorModel implements EditorModel {
    private final EditorContext<?> context;
    private final StringProperty valueProperty;
    private final ObservableBooleanValue validProperty;
    private final ObjectProperty<SNBTPreviewNode> previewRootProperty;

    public SNBTEditorModel(EditorContext<?> context) {
        this.context = context;
        valueProperty = StringProperty.create(context.getTag().toString());
        validProperty = valueProperty.mapToBoolean(value -> {
            try {
                return SnbtHelper.parse(value) != null;
            } catch (CommandSyntaxException e) {
                return false;
            }
        });
        previewRootProperty = ObjectProperty.create(SNBTPreviewNode.fromTag(context.getTag()));
        valueProperty.addListener(value -> refreshPreview());
    }

    public String getValue() {
        return valueProperty().getValue();
    }

    public StringProperty valueProperty() {
        return valueProperty;
    }

    public void setValue(String value) {
        valueProperty().setValue(value);
    }

    public ObjectProperty<SNBTPreviewNode> previewRootProperty() {
        return previewRootProperty;
    }

    private void refreshPreview() {
        try {
            previewRootProperty().setValue(SNBTPreviewNode.fromTag(SnbtHelper.parse(getValue())));
        } catch (CommandSyntaxException e) {
            previewRootProperty().setValue(null);
        }
    }

    @Override
    public void apply() {
        try {
            context.setTag(SnbtHelper.parse(getValue()));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
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
