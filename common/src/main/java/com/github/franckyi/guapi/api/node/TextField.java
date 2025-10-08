package com.github.franckyi.guapi.api.node;

import com.github.franckyi.databindings.api.*;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

public interface TextField extends Labeled {
    default String getText() {
        return textProperty().getValue();
    }

    StringProperty textProperty();

    default void setText(String value) {
        textProperty().setValue(value);
    }

    default int getMaxLength() {
        return maxLengthProperty().getValue();
    }

    IntegerProperty maxLengthProperty();

    default void setMaxLength(int value) {
        maxLengthProperty().setValue(value);
    }

    default Predicate<String> getValidator() {
        return validatorProperty().getValue();
    }

    ObjectProperty<Predicate<String>> validatorProperty();

    default void setValidator(Predicate<String> value) {
        validatorProperty().setValue(value);
    }

    default boolean isValidationForced() {
        return validationForcedProperty().getValue();
    }

    BooleanProperty validationForcedProperty();

    default void setValidationForced(boolean value) {
        validationForcedProperty().setValue(value);
    }

    default boolean isValid() {
        return validProperty().getValue();
    }

    ObservableBooleanValue validProperty();

    default TextRenderer getTextRenderer() {
        return textRendererProperty().getValue();
    }

    ObjectProperty<TextRenderer> textRendererProperty();

    default void setTextRenderer(TextRenderer value) {
        textRendererProperty().setValue(value);
    }

    default int getCursorPosition() {
        return cursorPositionProperty().getValue();
    }

    IntegerProperty cursorPositionProperty();

    default void setCursorPosition(int value) {
        cursorPositionProperty().setValue(value);
    }

    default int getHighlightPosition() {
        return highlightPositionProperty().getValue();
    }

    IntegerProperty highlightPositionProperty();

    default void setHighlightPosition(int value) {
        highlightPositionProperty().setValue(value);
    }

    ObjectProperty<TextFieldEventListener> onTextUpdateProperty();

    default TextFieldEventListener getOnTextUpdate() {
        return onTextUpdateProperty().getValue();
    }

    default void setOnTextUpdate(TextFieldEventListener listener) {
        onTextUpdateProperty().setValue(listener);
    }

    default void onTextUpdate(int oldCursorPos, int oldHighlightPos, String oldText, int newCursorPos, String newText) {
        TextFieldEventListener listener = getOnTextUpdate();
        if (listener != null) {
            listener.handle(oldCursorPos, oldHighlightPos, oldText, newCursorPos, newText);
        }
    }

    ObservableList<String> getSuggestions();

    default boolean isSuggested() {
        return suggestedProperty().getValue();
    }

    ObservableBooleanValue suggestedProperty();

    default Component getPlaceholder() {
        return placeholderProperty().getValue();
    }

    ObjectProperty<Component> placeholderProperty();

    default void setPlaceholder(Component value) {
        placeholderProperty().setValue(value);
    }

    @FunctionalInterface
    interface TextRenderer {
        Component render(String text, int firstCharacterIndex);
    }

    @FunctionalInterface
    interface TextFieldEventListener {
        void handle(int oldCursorPos, int oldHighlightPos, String oldText, int newCursorPos, String newText);
    }
}
