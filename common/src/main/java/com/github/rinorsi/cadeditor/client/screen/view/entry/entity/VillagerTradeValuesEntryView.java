package com.github.rinorsi.cadeditor.client.screen.view.entry.entity;

import com.github.franckyi.guapi.api.node.CheckBox;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.franckyi.guapi.api.util.Align;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

import static com.github.franckyi.guapi.api.GuapiHelper.CENTER_LEFT;
import static com.github.franckyi.guapi.api.GuapiHelper.checkBox;
import static com.github.franckyi.guapi.api.GuapiHelper.hBox;
import static com.github.franckyi.guapi.api.GuapiHelper.label;
import static com.github.franckyi.guapi.api.GuapiHelper.textField;
import static com.github.franckyi.guapi.api.GuapiHelper.vBox;

public class VillagerTradeValuesEntryView extends EntryView {
    private static final int SECTION_SPACING = 3;
    private static final int ROW_SPACING = 5;
    private static final int CONTENT_PADDING = 5;
    public static final int ENTRY_HEIGHT = VillagerTradeItemsEntryView.ENTRY_HEIGHT;
    private static final int LABEL_WIDTH = 110;
    private static final int FIELD_WIDTH = 150;
    private static final int ROW_HEIGHT = 24;

    private Label tradeTitleLabel;
    private TextField maxUsesField;
    private TextField usesField;
    private TextField demandField;
    private TextField specialPriceField;
    private TextField priceMultiplierField;
    private CheckBox rewardExpBox;
    private TextField xpField;

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
            root.add(createNumberRow(ModTexts.TRADE_MAX_USES.copy(), () -> maxUsesField = textField().prefWidth(FIELD_WIDTH)));
            root.add(createNumberRow(ModTexts.TRADE_USES.copy(), () -> usesField = textField().prefWidth(FIELD_WIDTH)));
            root.add(createNumberRow(ModTexts.TRADE_DEMAND.copy(), () -> demandField = textField().prefWidth(FIELD_WIDTH)));
            root.add(createNumberRow(ModTexts.TRADE_SPECIAL_PRICE.copy(), () -> specialPriceField = textField().prefWidth(FIELD_WIDTH)));
            root.add(createNumberRow(ModTexts.TRADE_PRICE_MULTIPLIER.copy(), () -> priceMultiplierField = textField().prefWidth(FIELD_WIDTH)));
            root.add(createToggleRow(ModTexts.TRADE_REWARD_EXP.copy(), () -> rewardExpBox = checkBox("")));
            root.add(createNumberRow(ModTexts.TRADE_XP.copy(), () -> xpField = textField().prefWidth(FIELD_WIDTH)));
            root.spacing(SECTION_SPACING).padding(CONTENT_PADDING);
        });
    }

    private Node createNumberRow(MutableComponent labelText, Supplier<TextField> fieldFactory) {
        return hBox(row -> {
            row.add(label(labelText).prefWidth(LABEL_WIDTH));
            row.add(fieldFactory.get());
            row.spacing(ROW_SPACING).align(CENTER_LEFT);
            row.prefHeight(ROW_HEIGHT);
            row.setMinHeight(ROW_HEIGHT);
        });
    }

    private Node createToggleRow(MutableComponent labelText, Supplier<Node> controlFactory) {
        return hBox(row -> {
            row.add(label(labelText).prefWidth(LABEL_WIDTH));
            row.add(controlFactory.get());
            row.spacing(ROW_SPACING).align(CENTER_LEFT);
            row.prefHeight(ROW_HEIGHT);
            row.setMinHeight(ROW_HEIGHT);
        });
    }

    public Label getTradeTitleLabel() {
        return tradeTitleLabel;
    }

    public TextField getMaxUsesField() {
        return maxUsesField;
    }

    public TextField getUsesField() {
        return usesField;
    }

    public TextField getDemandField() {
        return demandField;
    }

    public TextField getSpecialPriceField() {
        return specialPriceField;
    }

    public TextField getPriceMultiplierField() {
        return priceMultiplierField;
    }

    public CheckBox getRewardExpBox() {
        return rewardExpBox;
    }

    public TextField getXpField() {
        return xpField;
    }
}
