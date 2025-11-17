package com.github.rinorsi.cadeditor.client.screen.view.entry;

import com.github.franckyi.guapi.api.node.ItemView;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class EntityEntryView extends LabeledEntryView {
    private TextField entityField;
    private TexturedButton selectEntityButton;
    private TexturedButton pasteFromVaultButton;
    private TexturedButton openEditorButton;
    private TexturedButton openNbtEditorButton;
    private TexturedButton openSnbtEditorButton;
    private Label entityNameLabel;
    private ItemView entityIconView;

    @Override
    protected Node createLabeledContent() {
        return hBox(content -> {
            content.add(entityIconView = itemView().prefWidth(18).prefHeight(18));
            content.add(entityNameLabel = label());
            content.add(entityField = textField().prefHeight(16), 1);
            content.add(selectEntityButton = texturedButton(ModTextures.SEARCH, 16, 16, false)
                    .tooltip(ModTexts.SEARCH));
            content.add(pasteFromVaultButton = texturedButton(ModTextures.PASTE, 16, 16, false)
                    .tooltip(ModTexts.LOAD_VAULT));
            content.add(openEditorButton = texturedButton(ModTextures.EDITOR, 16, 16, false)
                    .tooltip(ModTexts.OPEN_EDITOR));
            content.add(openNbtEditorButton = texturedButton(ModTextures.NBT_EDITOR, 16, 16, false)
                    .tooltip(ModTexts.OPEN_NBT_EDITOR));
            content.add(openSnbtEditorButton = texturedButton(ModTextures.SNBT_EDITOR, 16, 16, false)
                    .tooltip(ModTexts.OPEN_SNBT_EDITOR));
            content.spacing(4).align(CENTER);
        });
    }

    public TextField getEntityField() {
        return entityField;
    }

    public TexturedButton getSelectEntityButton() {
        return selectEntityButton;
    }

    public TexturedButton getPasteFromVaultButton() {
        return pasteFromVaultButton;
    }

    public TexturedButton getOpenEditorButton() {
        return openEditorButton;
    }

    public TexturedButton getOpenNbtEditorButton() {
        return openNbtEditorButton;
    }

    public TexturedButton getOpenSnbtEditorButton() {
        return openSnbtEditorButton;
    }

    public Label getEntityNameLabel() {
        return entityNameLabel;
    }

    public ItemView getEntityIconView() {
        return entityIconView;
    }
}
