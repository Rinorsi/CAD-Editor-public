package com.github.rinorsi.cadeditor.client.util.texteditor;

public interface TextEditorActionHandler {
    void removeColorFormatting();

    void addColorFormatting(String color);

    void addStyleFormatting(StyleType type);
}
