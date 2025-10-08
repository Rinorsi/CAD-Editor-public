package com.github.rinorsi.cadeditor.client.theme;

import com.github.franckyi.guapi.api.node.*;
import com.github.franckyi.guapi.api.theme.Theme;
import com.github.franckyi.guapi.base.theme.AbstractTheme;
import com.github.franckyi.guapi.base.theme.vanilla.VanillaTheme;

public final class MonochromeTheme extends AbstractTheme {
    public static final Theme INSTANCE = new MonochromeTheme();

    private MonochromeTheme() {
        registerSkinInstance(Label.class, MonochromeLabelSkin.INSTANCE);
        registerSkinSupplier(Button.class, MonochromeButtonSkin::new);

        mirrorVanillaSkin(TexturedButton.class);
        mirrorVanillaSkin(EnumButton.class);
        mirrorVanillaSkin(TextField.class);
        mirrorVanillaSkin(CheckBox.class);
        mirrorVanillaSkin(ListView.class);
        mirrorVanillaSkin(TreeView.class);
        mirrorVanillaSkin(ToggleButton.class);
        mirrorVanillaSkin(TexturedToggleButton.class);
        mirrorVanillaSkin(Slider.class);
        mirrorVanillaSkin(TextArea.class);
        mirrorVanillaSkin(HBox.class);
        mirrorVanillaSkin(VBox.class);
        mirrorVanillaSkin(ImageView.class);
        mirrorVanillaSkin(ItemView.class);
        mirrorVanillaSkin(SpriteView.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <N extends Node> void mirrorVanillaSkin(Class type) {
        registerGenericSkinSupplier(type, node -> VanillaTheme.INSTANCE.supplySkin(node, type));
    }
}
