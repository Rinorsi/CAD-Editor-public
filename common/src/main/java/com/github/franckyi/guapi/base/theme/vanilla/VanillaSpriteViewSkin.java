package com.github.franckyi.guapi.base.theme.vanilla;

import com.github.franckyi.guapi.api.RenderHelper;
import com.github.franckyi.guapi.api.node.SpriteView;
import com.github.franckyi.guapi.api.theme.Skin;
import com.github.franckyi.guapi.base.theme.AbstractSkin;
import net.minecraft.client.gui.GuiGraphics;

public class VanillaSpriteViewSkin extends AbstractSkin<SpriteView> {
    public static final Skin<SpriteView> INSTANCE = new VanillaSpriteViewSkin();

    private VanillaSpriteViewSkin() {
    }

    @Override
    public void render(SpriteView node, GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(node, guiGraphics, mouseX, mouseY, delta);
        if (node.getSpriteFactory() != null) {
            RenderHelper.drawSprite(guiGraphics, node.getSprite(),
                    node.getX(), node.getY(), node.getImageWidth(), node.getImageHeight());
        }
    }

    @Override
    public int computeWidth(SpriteView node) {
        return node.getImageWidth() + node.getPadding().getHorizontal();
    }

    @Override
    public int computeHeight(SpriteView node) {
        return node.getImageHeight() + node.getPadding().getVertical();
    }
}
