package com.github.rinorsi.cadeditor.client.screen.controller.selection.color;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ColorSelectionScreenModel;
import com.github.rinorsi.cadeditor.client.screen.view.selection.color.PotionColorSelectionScreenView;
import com.github.rinorsi.cadeditor.common.ColoredItemHelper;
import net.minecraft.resources.ResourceLocation;

public class PotionColorSelectionScreenController extends ColorSelectionScreenController<PotionColorSelectionScreenView> {
    public PotionColorSelectionScreenController(ColorSelectionScreenModel model, PotionColorSelectionScreenView view) {
        super(model, view);
    }

    @Override
    protected void updateExample() {
        super.updateExample();
        view.getExamplePotion().setItem(ColoredItemHelper.createColoredPotionItem(ResourceLocation.parse("empty"),
                Color.fromRGB((int) model.getRedValue(), (int) model.getGreenValue(), (int) model.getBlueValue())));
    }
}
