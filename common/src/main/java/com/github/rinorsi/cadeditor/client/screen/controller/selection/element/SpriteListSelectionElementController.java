package com.github.rinorsi.cadeditor.client.screen.controller.selection.element;

import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SpriteListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.selection.element.SpriteListSelectionElementView;

public class SpriteListSelectionElementController extends ListSelectionElementController<SpriteListSelectionElementModel, SpriteListSelectionElementView> {
    public SpriteListSelectionElementController(SpriteListSelectionElementModel model, SpriteListSelectionElementView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getSpriteView().setSpriteFactory(model.getSpriteFactory());
    }
}

