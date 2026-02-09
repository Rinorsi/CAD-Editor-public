package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.CheckBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@SuppressWarnings("this-escape")
public class VanillaCheckBoxSkinDelegate extends AbstractButton implements VanillaWidgetSkinDelegate {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(com.github.rinorsi.cadeditor.common.ModConstants.MOD_ID, "textures/gui/checkbox.png");
    protected final CheckBox node;
    private boolean selected;

    public VanillaCheckBoxSkinDelegate(CheckBox node) {
        super(node.getX(), node.getY(), node.getWidth(), 16, node.getLabel());
        this.node = node;
        this.selected = node.isChecked();
        initLabeledWidget(node);
        node.checkedProperty().addListener(this::onModelChange);
    }

    private void onModelChange() {
        if (node.isChecked() != selected) {
            setSelected(node.isChecked());
        }
    }

    private void setSelected(boolean selected) {
        this.selected = selected;
    }

    private boolean isSelected() {
        return selected;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        setSelected(!isSelected());
        node.setChecked(isSelected());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), isHoveredOrFocused() ? 16f : 0f, isSelected() ? 16f : 0f, 16, 16, 32, 32);
        guiGraphics.drawString(mc.font, getMessage(), getX() + 20, getY() + (height - mc.font.lineHeight - 1) / 2, 14737632 | Mth.ceil(alpha * 255.0F) << 24);
    }
}
