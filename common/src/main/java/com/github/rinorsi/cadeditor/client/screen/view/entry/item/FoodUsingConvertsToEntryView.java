package com.github.rinorsi.cadeditor.client.screen.view.entry.item;

import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.ItemView;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.screen.view.entry.SelectionEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.itemView;
import static com.github.franckyi.guapi.api.GuapiHelper.texturedButton;

public class FoodUsingConvertsToEntryView extends SelectionEntryView {
    private ItemView itemPreview;
    private TexturedButton loadVaultButton;
    private TexturedButton openEditorButton;
    private TexturedButton openSnbtEditorButton;

    @Override
    public void build() {
        super.build();
        itemPreview = itemView().drawDecorations();
        setPreview(itemPreview);
        setPreviewVisible(false);
        HBox selectionBox = getSelectionBox();
        int searchIndex = selectionBox.getChildren().indexOf(getSelectionScreenButton());
        int insertIndex = searchIndex + 1;
        selectionBox.getChildren().add(insertIndex++, loadVaultButton = texturedButton(ModTextures.PASTE, 16, 16, false)
                .tooltip(ModTexts.LOAD_VAULT));
        selectionBox.getChildren().add(insertIndex++, openEditorButton = texturedButton(ModTextures.EDITOR, 16, 16, false)
                .tooltip(ModTexts.OPEN_EDITOR));
        selectionBox.getChildren().add(insertIndex, openSnbtEditorButton = texturedButton(ModTextures.SNBT_EDITOR, 16, 16, false)
                .tooltip(ModTexts.OPEN_SNBT_EDITOR));
    }

    public ItemView getItemPreview() {
        return itemPreview;
    }

    public TexturedButton getLoadVaultButton() {
        return loadVaultButton;
    }

    public TexturedButton getOpenEditorButton() {
        return openEditorButton;
    }

    public TexturedButton getOpenSnbtEditorButton() {
        return openSnbtEditorButton;
    }
}
