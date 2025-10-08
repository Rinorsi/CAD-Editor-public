package com.github.rinorsi.cadeditor.client.screen.view.selection.element;

import com.github.franckyi.guapi.api.node.CheckBox;

public class EnchantmentListSelectionElementView extends ItemListSelectionElementView {
    @Override
    public void build() {
        super.build();
        enableSelection();
        getRoot().setSpacing(5);
    }

    public CheckBox getCheckBox() {
        return getSelectionCheckBox();
    }

    @Override
    public void enableSelection() {
        super.enableSelection();
        getRoot().setSpacing(5);
    }
}
