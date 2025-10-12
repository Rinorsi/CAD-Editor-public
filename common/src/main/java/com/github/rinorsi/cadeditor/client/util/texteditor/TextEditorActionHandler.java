package com.github.rinorsi.cadeditor.client.util.texteditor;

public interface TextEditorActionHandler {
    void removeColorFormatting();

    void addColorFormatting(String color);

    void addStyleFormatting(StyleType type);

    default boolean supportsColorFormatting() {
        return true;
    }

    default boolean supportsColorReset() {
        return true;
    }

    default boolean supportsStyleFormatting() {
        return true;
    }

    default boolean supportsCustomColorPicker() {
        return true;
    }
}
