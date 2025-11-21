package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.franckyi.databindings.api.ObservableBooleanValue;
import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.StringProperty;
import com.github.rinorsi.cadeditor.client.context.EditorContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;

@SuppressWarnings("this-escape")
public class SNBTEditorModel implements EditorModel {
    private final EditorContext<?> context;
    private final StringProperty valueProperty;
    private final ObservableBooleanValue validProperty;
    private final ObjectProperty<SNBTPreviewNode> previewRootProperty;
    private final ObjectProperty<Integer> parseErrorIndexProperty;

    public SNBTEditorModel(EditorContext<?> context) {
        this.context = context;
        valueProperty = StringProperty.create(context.getTag().toString());
        validProperty = valueProperty.mapToBoolean(value -> {
            try {
                return TagParser.parseTag(value) != null;
            } catch (CommandSyntaxException e) {
                return false;
            }
        });
        previewRootProperty = ObjectProperty.create(SNBTPreviewNode.fromTag(context.getTag()));
        parseErrorIndexProperty = ObjectProperty.create(null);
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

    public ObjectProperty<Integer> parseErrorIndexProperty() {
        return parseErrorIndexProperty;
    }

    private void refreshPreview() {
        try {
            previewRootProperty().setValue(SNBTPreviewNode.fromTag(TagParser.parseTag(getValue())));
            parseErrorIndexProperty().setValue(null);
        } catch (CommandSyntaxException e) {
            previewRootProperty().setValue(null);
            parseErrorIndexProperty().setValue(clampCursorToValueLength(e.getCursor()));
        }
    }

    @Override
    public void apply() {
        try {
            context.setTag(TagParser.parseTag(getValue()));
            parseErrorIndexProperty().setValue(null);
        } catch (CommandSyntaxException e) {
            parseErrorIndexProperty().setValue(clampCursorToValueLength(e.getCursor()));
            throw new ParseException(e);
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

    private int clampCursorToValueLength(int cursor) {
        String value = getValue();
        if (value == null) {
            return Math.max(0, cursor);
        }
        int length = value.length();
        if (cursor < 0) {
            return 0;
        }
        if (cursor > length) {
            return length;
        }
        return cursor;
    }

    public static class ParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public ParseException(Throwable cause) {
            super(cause);
        }
    }
}
