package com.github.rinorsi.cadeditor.client.screen.view.selection.element;

import com.github.franckyi.guapi.api.node.Button;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.button;

public class SoundEventListSelectionElementView extends ListSelectionElementView {
    private Button previewButton;

    @Override
    public void build() {
        super.build();
        getRoot().getChildren().add(previewButton = button("▶")
                .tooltip(ModTexts.gui("preview_sound"))
                .minSize(20, 16)
                .prefSize(20, 16)
                .maxSize(20, 16));
    }

    public Button getPreviewButton() {
        return previewButton;
    }
}
