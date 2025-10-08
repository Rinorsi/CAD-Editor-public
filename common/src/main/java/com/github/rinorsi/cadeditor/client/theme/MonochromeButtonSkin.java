package com.github.rinorsi.cadeditor.client.theme;

import com.github.franckyi.guapi.api.node.Button;
import com.github.franckyi.guapi.base.theme.vanilla.VanillaButtonSkin;

public class MonochromeButtonSkin<N extends Button> extends VanillaButtonSkin<N> {
    public MonochromeButtonSkin(N node) {
        super(node, new MonochromeButtonSkinDelegate<>(node));
    }
}
