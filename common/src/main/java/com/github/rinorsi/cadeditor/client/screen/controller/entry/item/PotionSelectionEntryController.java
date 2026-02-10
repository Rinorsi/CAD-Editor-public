package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.SelectionEntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ColorSelectionScreenModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.PotionSelectionEntryView;
import com.github.rinorsi.cadeditor.common.ColoredItemHelper;
import net.minecraft.resources.Identifier;

public class PotionSelectionEntryController extends SelectionEntryController<PotionSelectionEntryModel, PotionSelectionEntryView> {
    public PotionSelectionEntryController(PotionSelectionEntryModel model, PotionSelectionEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getChooseColorButton().onAction(() -> ModScreenHandler.openColorSelectionScreen(ColorSelectionScreenModel.Target.POTION, model.getCustomColor(), this::updatePotionColor));
        view.getRemoveColorButton().onAction(() -> model.setCustomColor(Color.NONE));
        view.getResetColorButton().onAction(model::resetCustomColor);
        model.customColorProperty().addListener(this::updatePotionItem);
        model.valueProperty().addListener(this::updatePotionItem);
        updatePotionItem();
    }

    private void updatePotionItem() {
        view.getPotionView().setItem(ColoredItemHelper.createColoredPotionItem(Identifier.tryParse(model.getValue()), model.getCustomColor()));
    }

    private void updatePotionColor(String value) {
        model.setCustomColor(Color.fromHex(value));
    }
}
