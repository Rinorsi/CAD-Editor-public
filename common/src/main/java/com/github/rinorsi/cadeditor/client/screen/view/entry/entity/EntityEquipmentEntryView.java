package com.github.rinorsi.cadeditor.client.screen.view.entry.entity;

import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.ItemView;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class EntityEquipmentEntryView extends EntryView {
    private Label slotLabel;
    private ItemView itemView;
    private Label itemNameLabel;
    private TextField dropChanceField;
    private TexturedButton chooseItemButton;
    private TexturedButton loadVaultButton;
    private TexturedButton clearButton;
    private TexturedButton openEditorButton;
    private TexturedButton openNbtEditorButton;
    private TexturedButton openSnbtEditorButton;

    @Override
    protected Node createContent() {
        return hBox(content -> {
            content.add(slotLabel = label().prefWidth(70));
            content.add(hBox(itemBox -> {
                itemBox.add(itemView = itemView().drawDecorations());
                itemBox.add(itemNameLabel = label().prefWidth(140));
                itemBox.spacing(6).align(CENTER_LEFT);
            }), 1);
            content.add(hBox(chanceBox -> {
                chanceBox.add(label(ModTexts.DROP_CHANCE.copy().withStyle(ChatFormatting.GRAY)));
                chanceBox.add(dropChanceField = textField().prefWidth(60));
                chanceBox.spacing(4).align(CENTER_LEFT);
            }));
            content.spacing(8).align(CENTER_LEFT);
        });
    }

    @Override
    public void build() {
        super.build();
        HBox buttonBox = getButtonBox();
        buttonBox.getChildren().add(chooseItemButton = texturedButton(ModTextures.SEARCH, 16, 16, false).tooltip(ModTexts.CHOOSE_ITEM));
        buttonBox.getChildren().add(loadVaultButton = texturedButton(ModTextures.PASTE, 16, 16, false).tooltip(ModTexts.LOAD_VAULT));
        buttonBox.getChildren().add(openEditorButton = texturedButton(ModTextures.EDITOR, 16, 16, false).tooltip(ModTexts.OPEN_EDITOR));
        buttonBox.getChildren().add(openSnbtEditorButton = texturedButton(ModTextures.SNBT_EDITOR, 16, 16, false).tooltip(ModTexts.OPEN_SNBT_EDITOR));
        buttonBox.getChildren().add(clearButton = texturedButton(ModTextures.REMOVE, 16, 16, false).tooltip(ModTexts.REMOVE));
    }

    public Label getSlotLabel() {
        return slotLabel;
    }

    public ItemView getItemView() {
        return itemView;
    }

    public Label getItemNameLabel() {
        return itemNameLabel;
    }

    public TextField getDropChanceField() {
        return dropChanceField;
    }

    public TexturedButton getClearButton() {
        return clearButton;
    }

    public TexturedButton getOpenEditorButton() {
        return openEditorButton;
    }
    public TexturedButton getChooseItemButton() {
        return chooseItemButton;
    }

    public TexturedButton getLoadVaultButton() {
        return loadVaultButton;
    }

    public TexturedButton getOpenNbtEditorButton() {
        return openNbtEditorButton;
    }

    public TexturedButton getOpenSnbtEditorButton() {
        return openSnbtEditorButton;
    }
}
