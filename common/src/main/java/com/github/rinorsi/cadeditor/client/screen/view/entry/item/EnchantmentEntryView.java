package com.github.rinorsi.cadeditor.client.screen.view.entry.item;

import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.ItemView;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.franckyi.guapi.api.node.builder.HBoxBuilder;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.screen.view.entry.SelectionEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class EnchantmentEntryView extends SelectionEntryView {
    private TextField levelField;
    private TexturedButton plusButton, minusButton;
    private ItemView previewItemView;
    private Label previewLabel;
    private HBox previewBox;

    @Override
    public void build() {
        super.build();
        getTextField().setPlaceholder(ModTexts.ENCHANTMENT);
        previewBox = hBox(preview -> {
            preview.add(previewItemView = itemView());
            preview.add(previewLabel = label().prefHeight(16));
            preview.align(CENTER_LEFT).spacing(4);
        });
        setPreview(previewBox);
        setPreviewVisible(false);
    }

    @Override
    protected Node createLabeledContent() {
        HBoxBuilder box = (HBoxBuilder) super.createLabeledContent();
        box.add(levelField = textField().prefSize(30, 16).tooltip(ModTexts.LEVEL));
        box.add(vBox(2,
                plusButton = texturedButton(ModTextures.LEVEL_ADD, 11, 7, false)
                        .tooltip(ModTexts.LEVEL_ADD),
                minusButton = texturedButton(ModTextures.LEVEL_REMOVE, 11, 7, false)
                        .tooltip(ModTexts.LEVEL_REMOVE)
        ));
        return box;
    }

    public TextField getLevelField() {
        return levelField;
    }

    public TexturedButton getPlusButton() {
        return plusButton;
    }

    public TexturedButton getMinusButton() {
        return minusButton;
    }

    public ItemView getPreviewItemView() {
        return previewItemView;
    }

    public Label getPreviewLabel() {
        return previewLabel;
    }

    public HBox getPreviewBox() {
        return previewBox;
    }
}
