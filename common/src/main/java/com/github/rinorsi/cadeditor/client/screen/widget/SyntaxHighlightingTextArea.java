package com.github.rinorsi.cadeditor.client.screen.widget;

import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.theme.Skin;
import com.github.franckyi.guapi.base.node.TextAreaImpl;
import com.github.rinorsi.cadeditor.client.screen.skin.SyntaxHighlightingTextAreaSkin;
import com.github.rinorsi.cadeditor.client.util.texteditor.SNBTSyntaxHighlighter;

/**
 * Text area widget that owns a {@link SNBTSyntaxHighlighter} instance and uses a custom skin
 * capable of rendering coloured tokens.
 */
public class SyntaxHighlightingTextArea extends TextAreaImpl {
    private final SNBTSyntaxHighlighter highlighter = new SNBTSyntaxHighlighter();

    public SyntaxHighlightingTextArea() {
        this("");
    }

    @SuppressWarnings({"unchecked", "this-escape"})
    public SyntaxHighlightingTextArea(String value) {
        super(value);
        highlighter.setSource(value);
        textProperty().addListener(highlighter::setSource);
        skin = (Skin<? super Node>) (Skin<?>) new SyntaxHighlightingTextAreaSkin(this);
    }

    @Override
    protected Class<?> getType() {
        return SyntaxHighlightingTextArea.class;
    }

    public SNBTSyntaxHighlighter getHighlighter() {
        return highlighter;
    }
}
