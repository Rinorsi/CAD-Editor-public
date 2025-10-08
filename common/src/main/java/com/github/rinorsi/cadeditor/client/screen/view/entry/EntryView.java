package com.github.rinorsi.cadeditor.client.screen.view.entry;

import com.github.franckyi.guapi.api.mvc.View;
import com.github.franckyi.guapi.api.node.Box;
import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public abstract class EntryView implements View {
    private HBox root, right, buttonBox, listButtons;
    private TexturedButton upButton, downButton, deleteButton, resetButton;

    @Override
    public void build() {
        listButtons = hBox(listButtons -> {
            listButtons.add(upButton = texturedButton(ModTextures.MOVE_UP, 16, 16, false)
                    .tooltip(ModTexts.MOVE_UP));
            listButtons.add(downButton = texturedButton(ModTextures.MOVE_DOWN, 16, 16, false)
                    .tooltip(ModTexts.MOVE_DOWN));
            listButtons.add(deleteButton = texturedButton(ModTextures.REMOVE, 16, 16, false)
                    .tooltip(ModTexts.REMOVE));
            listButtons.spacing(2);
        });
        root = hBox(root -> {
            root.add(createContent(), 1);
            root.add(right = hBox(right -> {
                right.add(buttonBox = hBox(buttons -> {
                    buttons.add(resetButton = texturedButton(ModTextures.RESET, 16, 16, false)
                            .tooltip(ModTexts.RESET));
                    buttons.spacing(2);
                }));
                right.spacing(5).align(CENTER_RIGHT);
            }));
            root.spacing(5).align(CENTER);
        });
    }

    protected abstract Node createContent();

    public void setListButtonsVisible(boolean visible) {
        if (visible && buttonBox.getChildren().size() <= 1) {
            buttonBox.getChildren().add(0, listButtons);
        } else if (!visible && buttonBox.getChildren().size() > 1) {
            buttonBox.getChildren().remove(0);
        }
    }

    protected HBox getRight() {
        return right;
    }

    @Override
    public Box getRoot() {
        return root;
    }

    public HBox getButtonBox() {
        return buttonBox;
    }

    public TexturedButton getUpButton() {
        return upButton;
    }

    public TexturedButton getDownButton() {
        return downButton;
    }

    public TexturedButton getDeleteButton() {
        return deleteButton;
    }

    public TexturedButton getResetButton() {
        return resetButton;
    }
}
