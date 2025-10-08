package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.CheckBox;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
    public void onPress() {
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
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        guiGraphics.blit(TEXTURE, getX(), getY(), isHoveredOrFocused() ? 16 : 0, isSelected() ? 16 : 0, 16, 16, 32, 32);
        guiGraphics.drawString(mc.font, getMessage(), getX() + 20, getY() + (height - mc.font.lineHeight - 1) / 2, 14737632 | Mth.ceil(alpha * 255.0F) << 24);
    }
}
