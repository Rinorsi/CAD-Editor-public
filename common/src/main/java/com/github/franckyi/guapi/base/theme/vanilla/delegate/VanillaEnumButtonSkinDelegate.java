package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.EnumButton;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;

public class VanillaEnumButtonSkinDelegate extends VanillaButtonSkinDelegate<EnumButton<?>> {
    public VanillaEnumButtonSkinDelegate(EnumButton<?> node) {
        super(node);
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return super.isValidClickButton(buttonInfo) || buttonInfo.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }
}
