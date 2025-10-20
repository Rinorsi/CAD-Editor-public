package com.github.rinorsi.cadeditor.client.screen.view.entry.entity;

import com.github.franckyi.guapi.api.node.ItemView;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.franckyi.guapi.api.node.builder.HBoxBuilder;
import com.github.franckyi.guapi.api.util.Align;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.franckyi.guapi.api.GuapiHelper.CENTER_LEFT;
import static com.github.franckyi.guapi.api.GuapiHelper.hBox;
import static com.github.franckyi.guapi.api.GuapiHelper.itemView;
import static com.github.franckyi.guapi.api.GuapiHelper.label;
import static com.github.franckyi.guapi.api.GuapiHelper.texturedButton;
import static com.github.franckyi.guapi.api.GuapiHelper.vBox;

public class VillagerTradeItemsEntryView extends EntryView {
    private static final int SECTION_SPACING = 20;
    private static final int ROW_SPACING = 5;
    private static final int CONTENT_PADDING = 5;
    public static final int ENTRY_HEIGHT = 210;
    private static final int BUTTON_GROUP_WIDTH = 120;
    private static final int NAME_LABEL_WIDTH = 100;
    private static final int ROW_HEIGHT = 24;

    private Label tradeTitleLabel;

    private ItemView primaryItemView;
    private Label primaryItemNameLabel;
    private TexturedButton primaryChooseButton;
    private TexturedButton primaryEditButton;
    private TexturedButton primarySnbtButton;
    private TexturedButton primaryClearButton;
    private TexturedButton primaryVaultButton;

    private ItemView secondaryItemView;
    private Label secondaryItemNameLabel;
    private TexturedButton secondaryChooseButton;
    private TexturedButton secondaryEditButton;
    private TexturedButton secondarySnbtButton;
    private TexturedButton secondaryClearButton;
    private TexturedButton secondaryVaultButton;

    private ItemView resultItemView;
    private Label resultItemNameLabel;
    private TexturedButton resultChooseButton;
    private TexturedButton resultEditButton;
    private TexturedButton resultSnbtButton;
    private TexturedButton resultClearButton;
    private TexturedButton resultVaultButton;

    @Override
    public void build() {
        super.build();
        getRoot().setAlignment(Align.TOP_LEFT);
        getRoot().setSpacing(4);
        getRoot().setMinHeight(ENTRY_HEIGHT);
        getRoot().setPrefHeight(ENTRY_HEIGHT);
        getRoot().setMaxHeight(ENTRY_HEIGHT);
        getRight().setAlignment(Align.TOP_RIGHT);
    }

    @Override
    protected Node createContent() {
        return vBox(root -> {
            root.add(tradeTitleLabel = label().prefWidth(0));
            root.add(createItemSection(ModTexts.TRADE_INPUT_PRIMARY.copy(),
                    () -> primaryItemView = itemView().prefWidth(ROW_HEIGHT).prefHeight(ROW_HEIGHT).drawDecorations(),
                    () -> primaryItemNameLabel = label().prefWidth(0),
                    buttons -> {
                        buttons.add(primaryVaultButton = texturedButton(ModTextures.PASTE, 16, 16, false).tooltip(ModTexts.LOAD_VAULT));
                        buttons.add(primaryChooseButton = texturedButton(ModTextures.SEARCH, 16, 16, false).tooltip(ModTexts.CHOOSE_ITEM));
                        buttons.add(primaryEditButton = texturedButton(ModTextures.EDITOR, 16, 16, false).tooltip(ModTexts.OPEN_EDITOR));
                        buttons.add(primarySnbtButton = texturedButton(ModTextures.SNBT_EDITOR, 16, 16, false).tooltip(ModTexts.OPEN_SNBT_EDITOR));
                        buttons.add(primaryClearButton = texturedButton(ModTextures.REMOVE, 16, 16, false).tooltip(ModTexts.REMOVE));
                    }));
            root.add(createItemSection(ModTexts.TRADE_INPUT_SECONDARY.copy(),
                    () -> secondaryItemView = itemView().prefWidth(ROW_HEIGHT).prefHeight(ROW_HEIGHT).drawDecorations(),
                    () -> secondaryItemNameLabel = label().prefWidth(0),
                    buttons -> {
                        buttons.add(secondaryVaultButton = texturedButton(ModTextures.PASTE, 16, 16, false).tooltip(ModTexts.LOAD_VAULT));
                        buttons.add(secondaryChooseButton = texturedButton(ModTextures.SEARCH, 16, 16, false).tooltip(ModTexts.CHOOSE_ITEM));
                        buttons.add(secondaryEditButton = texturedButton(ModTextures.EDITOR, 16, 16, false).tooltip(ModTexts.OPEN_EDITOR));
                        buttons.add(secondarySnbtButton = texturedButton(ModTextures.SNBT_EDITOR, 16, 16, false).tooltip(ModTexts.OPEN_SNBT_EDITOR));
                        buttons.add(secondaryClearButton = texturedButton(ModTextures.REMOVE, 16, 16, false).tooltip(ModTexts.REMOVE));
                    }));
            root.add(createItemSection(ModTexts.TRADE_OUTPUT.copy(),
                    () -> resultItemView = itemView().prefWidth(ROW_HEIGHT).prefHeight(ROW_HEIGHT).drawDecorations(),
                    () -> resultItemNameLabel = label().prefWidth(0),
                    buttons -> {
                        buttons.add(resultVaultButton = texturedButton(ModTextures.PASTE, 16, 16, false).tooltip(ModTexts.LOAD_VAULT));
                        buttons.add(resultChooseButton = texturedButton(ModTextures.SEARCH, 16, 16, false).tooltip(ModTexts.CHOOSE_ITEM));
                        buttons.add(resultEditButton = texturedButton(ModTextures.EDITOR, 16, 16, false).tooltip(ModTexts.OPEN_EDITOR));
                        buttons.add(resultSnbtButton = texturedButton(ModTextures.SNBT_EDITOR, 16, 16, false).tooltip(ModTexts.OPEN_SNBT_EDITOR));
                        buttons.add(resultClearButton = texturedButton(ModTextures.REMOVE, 16, 16, false).tooltip(ModTexts.REMOVE));
                    }));
            root.spacing(SECTION_SPACING).padding(CONTENT_PADDING);
        });
    }

    private Node createItemSection(MutableComponent title, Supplier<ItemView> itemViewFactory,
                                   Supplier<Label> nameLabelFactory, Consumer<HBoxBuilder> buttonsBuilder) {
        return vBox(section -> {
            section.add(label(title).prefWidth(0));
            section.add(hBox(row -> {
                row.add(itemViewFactory.get());
                Label nameLabel = nameLabelFactory.get();
                nameLabel.setPrefWidth(NAME_LABEL_WIDTH);
                nameLabel.setMaxWidth(NAME_LABEL_WIDTH);
                row.add(nameLabel, 0);
                row.add(hBox(), 1);
                row.add(hBox(buttonRow -> {
                    buttonsBuilder.accept(buttonRow);
                    buttonRow.spacing(2).align(Align.CENTER_RIGHT);
                }).prefWidth(BUTTON_GROUP_WIDTH).padding(0, 0, 0, 8));
                row.spacing(ROW_SPACING).align(CENTER_LEFT);
                row.prefHeight(ROW_HEIGHT);
            }));
            section.spacing(ROW_SPACING);
        });
    }

    public Label getTradeTitleLabel() {
        return tradeTitleLabel;
    }

    public ItemView getPrimaryItemView() {
        return primaryItemView;
    }

    public Label getPrimaryItemNameLabel() {
        return primaryItemNameLabel;
    }

    public TexturedButton getPrimaryChooseButton() {
        return primaryChooseButton;
    }

    public TexturedButton getPrimaryEditButton() {
        return primaryEditButton;
    }

    public TexturedButton getPrimarySnbtButton() {
        return primarySnbtButton;
    }

    public TexturedButton getPrimaryClearButton() {
        return primaryClearButton;
    }

    public TexturedButton getPrimaryVaultButton() {
        return primaryVaultButton;
    }

    public ItemView getSecondaryItemView() {
        return secondaryItemView;
    }

    public Label getSecondaryItemNameLabel() {
        return secondaryItemNameLabel;
    }

    public TexturedButton getSecondaryChooseButton() {
        return secondaryChooseButton;
    }

    public TexturedButton getSecondaryEditButton() {
        return secondaryEditButton;
    }

    public TexturedButton getSecondarySnbtButton() {
        return secondarySnbtButton;
    }

    public TexturedButton getSecondaryClearButton() {
        return secondaryClearButton;
    }

    public TexturedButton getSecondaryVaultButton() {
        return secondaryVaultButton;
    }

    public ItemView getResultItemView() {
        return resultItemView;
    }

    public Label getResultItemNameLabel() {
        return resultItemNameLabel;
    }

    public TexturedButton getResultChooseButton() {
        return resultChooseButton;
    }

    public TexturedButton getResultEditButton() {
        return resultEditButton;
    }

    public TexturedButton getResultSnbtButton() {
        return resultSnbtButton;
    }

    public TexturedButton getResultClearButton() {
        return resultClearButton;
    }

    public TexturedButton getResultVaultButton() {
        return resultVaultButton;
    }
}
