package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.franckyi.databindings.api.IntegerProperty;
import com.github.franckyi.databindings.api.ObservableBooleanValue;
import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.StringProperty;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.EditorContext;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("this-escape")
public class SNBTEditorModel implements EditorModel {
    private static final Logger LOGGER = LogManager.getLogger();
    private final EditorContext<?> context;
    private final StringProperty valueProperty;
    private final ObservableBooleanValue validProperty;
    private final ObjectProperty<SNBTPreviewNode> previewRootProperty;
    private final IntegerProperty errorCursorProperty;

    public SNBTEditorModel(EditorContext<?> context) {
        this.context = context;
        valueProperty = StringProperty.create(context.getTag().toString());
        errorCursorProperty = IntegerProperty.create(-1);
        validProperty = valueProperty.mapToBoolean(value -> {
            try {
                TagParser.parseTag(value);
                errorCursorProperty.setValue(-1);
                return true;
            } catch (CommandSyntaxException e) {
                errorCursorProperty.setValue(e.getCursor());
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

    public IntegerProperty errorCursorProperty() {
        return errorCursorProperty;
    }

    private void refreshPreview() {
        try {
            previewRootProperty().setValue(SNBTPreviewNode.fromTag(TagParser.parseTag(getValue())));
            errorCursorProperty.setValue(-1);
        } catch (CommandSyntaxException e) {
            previewRootProperty().setValue(null);
            errorCursorProperty.setValue(e.getCursor());
        }
    }

    @Override
    public void apply() {
        try {
            context.setTag(TagParser.parseTag(getValue()));
            errorCursorProperty.setValue(-1);
        } catch (CommandSyntaxException e) {
            LOGGER.error("Failed to parse SNBT payload", e);
            ClientUtil.showMessage(ModTexts.Messages.ERROR_GENERIC);
            errorCursorProperty.setValue(e.getCursor());
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
