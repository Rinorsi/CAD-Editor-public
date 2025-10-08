package com.github.rinorsi.cadeditor.client.screen.view.entry.item;

import com.github.franckyi.guapi.api.node.EnumButton;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.franckyi.guapi.api.util.Predicates;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ToolRuleEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class ToolRuleEntryView extends EntryView {
    private Label ruleLabel;
    private TextField selectionField;
    private TexturedButton selectBlocksButton;
    private TexturedButton selectTagsButton;
    private TexturedButton deleteRuleButton;
    private TextField speedField;
    private EnumButton<ToolRuleEntryModel.DropBehavior> behaviorButton;

    @Override
    protected Node createContent() {
        return hBox(content -> {
            content.add(ruleLabel = label()
                    .prefWidth(26)
                    .prefHeight(16)
                    .textAlign(CENTER_RIGHT));
            content.add(hBox(selection -> {
                selection.add(selectionField = textField()
                        .prefHeight(16)
                        .prefWidth(0)
                        .minWidth(60)
                        .maxWidth(Node.INFINITE_SIZE)
                        .padding(0, 2)
                        .placeholder(ModTexts.gui("tool_rule_blocks_empty")), 1);
                selection.spacing(4).align(CENTER_LEFT);
            }), 1);
            content.add(speedField = textField()
                    .prefWidth(60)
                    .prefHeight(16)
                    .validator(value -> value == null || value.isBlank() || Predicates.IS_DOUBLE.test(value))
                    .placeholder(ModTexts.gui("tool_rule_speed")));
            content.add(behaviorButton = enumButton(ToolRuleEntryModel.DropBehavior.values())
                    .prefWidth(60)
                    .prefHeight(16));
            content.spacing(6).align(CENTER_LEFT);
        });
    }

    @Override
    public void build() {
        super.build();
        var buttonBox = getButtonBox();
        int resetIndex = buttonBox.getChildren().indexOf(getResetButton());
        if (resetIndex < 0) {
            resetIndex = buttonBox.getChildren().size();
        }
        selectBlocksButton = texturedButton(ModTextures.SEARCH, 16, 16, false)
                .tooltip(ModTexts.choose(ModTexts.gui("tool_rule_blocks")));
        selectTagsButton = texturedButton(ModTextures.SEARCH, 16, 16, false)
                .tooltip(ModTexts.choose(ModTexts.gui("tool_rule_tags")));
        deleteRuleButton = texturedButton(ModTextures.REMOVE, 16, 16, false)
                .tooltip(ModTexts.REMOVE);
        buttonBox.getChildren().add(resetIndex, selectBlocksButton);
        buttonBox.getChildren().add(resetIndex + 1, selectTagsButton);
        buttonBox.getChildren().add(deleteRuleButton);
    }

    public TextField getSelectionField() {
        return selectionField;
    }

    public Label getRuleLabel() {
        return ruleLabel;
    }

    public TextField getSpeedField() {
        return speedField;
    }

    public EnumButton<ToolRuleEntryModel.DropBehavior> getBehaviorButton() {
        return behaviorButton;
    }

    public TexturedButton getSelectBlocksButton() {
        return selectBlocksButton;
    }

    public TexturedButton getSelectTagsButton() {
        return selectTagsButton;
    }

    public TexturedButton getDeleteRuleButton() {
        return deleteRuleButton;
    }
}
