package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.rinorsi.cadeditor.client.screen.controller.entry.BooleanEntryController;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemHideFlagsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.HideFlagEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.HideFlagEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;

public class HideFlagEntryController extends BooleanEntryController<HideFlagEntryModel, HideFlagEntryView> {
    public HideFlagEntryController(HideFlagEntryModel model, HideFlagEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        if (model.getHideFlag() == ItemHideFlagsCategoryModel.HideFlag.OTHER) {
            view.getLabel().getTooltip().addAll(ModTexts.HIDE_OTHER_TOOLTIP);
        }
    }
}
