package com.github.rinorsi.cadeditor.client.screen.controller.selection.element;

import com.github.rinorsi.cadeditor.client.screen.model.selection.element.EnchantmentListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.selection.element.ItemListSelectionElementView;

public class ItemListSelectionElementController<M extends ItemListSelectionElementModel, V extends ItemListSelectionElementView> extends ListSelectionElementController<M, V> {
    public ItemListSelectionElementController(M model, V view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getItemView().setItem(model.getItem());
        if (model instanceof EnchantmentListSelectionElementModel) view.getItemView().setDrawTooltip(false);
    }
}
