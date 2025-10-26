package com.github.rinorsi.cadeditor.client.screen.skin;

import com.github.franckyi.guapi.base.theme.vanilla.AbstractVanillaWidgetSkin;
import com.github.rinorsi.cadeditor.client.screen.widget.SyntaxHighlightingTextArea;

public class SyntaxHighlightingTextAreaSkin extends AbstractVanillaWidgetSkin<SyntaxHighlightingTextArea, SyntaxHighlightingTextAreaSkinDelegate> {
    public SyntaxHighlightingTextAreaSkin(SyntaxHighlightingTextArea node) {
        super(node, new SyntaxHighlightingTextAreaSkinDelegate(node));
    }

    @Override
    public int computeWidth(SyntaxHighlightingTextArea node) {
        return 150;
    }

    @Override
    public int computeHeight(SyntaxHighlightingTextArea node) {
        return 20;
    }
}
