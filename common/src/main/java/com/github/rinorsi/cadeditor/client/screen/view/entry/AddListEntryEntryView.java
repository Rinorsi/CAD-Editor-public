package com.github.rinorsi.cadeditor.client.screen.view.entry;

import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.franckyi.guapi.api.util.Align;
import com.github.franckyi.guapi.api.util.Insets;
import com.github.rinorsi.cadeditor.client.ModTextures;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class AddListEntryEntryView extends EntryView {
    private TexturedButton button;
    private HBox contentContainer;

    @Override
    public void build() {
        super.build();
        getRoot().setAlignment(Align.TOP_CENTER);
        getRoot().setPadding(new Insets(4, 0, 4, 0));
    }

    @Override
    protected Node createContent() {
        contentContainer = hBox(button = texturedButton(ModTextures.ADD, 16, 16, false))
                .prefHeight(16)
                .align(Align.CENTER);
        return contentContainer;
    }

    public TexturedButton getButton() {
        return button;
    }

    public HBox getContentContainer() {
        return contentContainer;
    }
}
