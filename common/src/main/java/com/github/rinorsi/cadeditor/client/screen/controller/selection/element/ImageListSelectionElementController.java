package com.github.rinorsi.cadeditor.client.screen.controller.selection.element;

import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ImageListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.selection.element.ImageListSelectionElementView;

public class ImageListSelectionElementController extends ListSelectionElementController<ImageListSelectionElementModel, ImageListSelectionElementView> {
    public ImageListSelectionElementController(ImageListSelectionElementModel model, ImageListSelectionElementView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getImageView().setTextureId(model.getTextureId());
    }
}
