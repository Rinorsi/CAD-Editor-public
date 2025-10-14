package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.LabeledEntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FireworkColorEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ColorSelectionScreenModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.FireworkColorEntryView;
import com.github.rinorsi.cadeditor.common.ColoredItemHelper;
import net.minecraft.network.chat.Component;

public class FireworkColorEntryController extends LabeledEntryController<FireworkColorEntryModel, FireworkColorEntryView> {
    public FireworkColorEntryController(FireworkColorEntryModel model, FireworkColorEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getChooseColorButton().onAction(() -> ModScreenHandler.openColorSelectionScreen(
                ColorSelectionScreenModel.Target.TEXT, model.getValue(), hex -> model.setValue(Color.fromHex(hex))));
        view.getRemoveColorButton().onAction(model::remove);
        model.valueProperty().addListener(this::updateColorPreview);
        updateColorPreview();
    }

    private void updateColorPreview() {
        int color = model.getValue();
        view.getPreviewItem().setItem(ColoredItemHelper.createFireworkStarItem(color));
        Component label = model.hasCustomColor()
                ? Component.literal(String.format("#%06X", color & 0xFFFFFF))
                : Component.literal("-");
        view.getHexLabel().setLabel(label);
    }
}
