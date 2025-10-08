package com.github.rinorsi.cadeditor.client.screen.controller.selection.element;

import static com.github.franckyi.guapi.api.GuapiHelper.text;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SortedEnchantmentListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.selection.element.EnchantmentListSelectionElementView;

import net.minecraft.ChatFormatting;

public class SortedEnchantmentListSelectionElementController extends ItemListSelectionElementController<SortedEnchantmentListSelectionElementModel, EnchantmentListSelectionElementView> {
    public SortedEnchantmentListSelectionElementController(SortedEnchantmentListSelectionElementModel model, EnchantmentListSelectionElementView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getRoot().getTooltip().clear();
        view.getRoot().getTooltip().add(text(model.getId().toString()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        if (!model.getCategoryLabel().getString().isEmpty()) {
            view.getRoot().getTooltip().add(model.getCategoryLabel().copy().withStyle(ChatFormatting.GRAY));
        }

    }
}
