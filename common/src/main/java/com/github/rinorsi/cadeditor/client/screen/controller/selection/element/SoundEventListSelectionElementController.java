package com.github.rinorsi.cadeditor.client.screen.controller.selection.element;

import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SoundEventListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.selection.element.SoundEventListSelectionElementView;
import net.minecraft.client.Minecraft;

public class SoundEventListSelectionElementController
        extends ListSelectionElementController<SoundEventListSelectionElementModel, SoundEventListSelectionElementView> {
    public SoundEventListSelectionElementController(SoundEventListSelectionElementModel model, SoundEventListSelectionElementView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getPreviewButton().onAction(this::playPreviewSound);
    }

    private void playPreviewSound() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || model.getSoundEvent() == null) {
            return;
        }
        minecraft.player.playSound(model.getSoundEvent(), 1.0F, 1.0F);
    }
}
