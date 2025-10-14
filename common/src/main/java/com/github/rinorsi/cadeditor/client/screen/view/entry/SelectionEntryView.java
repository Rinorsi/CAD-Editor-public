package com.github.rinorsi.cadeditor.client.screen.view.entry;

import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.rinorsi.cadeditor.client.ModTextures;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class SelectionEntryView extends StringEntryView {
    private TexturedButton selectionScreenButton;
    private HBox selectionBox;
    private Node previewNode;

    @Override
    protected Node createLabeledContent() {
        return selectionBox = hBox(box -> {
            box.add(super.createLabeledContent(), 1);
            box.add(selectionScreenButton = texturedButton(ModTextures.SEARCH, 16, 16, false));
            box.align(CENTER).spacing(4);
        });
    }

    protected HBox getSelectionBox() {
        return selectionBox;
    }

    public TexturedButton getSelectionScreenButton() {
        return selectionScreenButton;
    }

    public void setPreview(Node node) {
        if (previewNode != null) {
            selectionBox.getChildren().remove(previewNode);
        }
        previewNode = node;
        if (previewNode != null) {
            previewNode.setVisible(false);
        }
    }

    public void setPreviewVisible(boolean visible) {
        if (previewNode == null) {
            return;
        }
        var children = selectionBox.getChildren();
        boolean contains = children.contains(previewNode);
        if (visible && !contains) {
            int index = children.indexOf(selectionScreenButton);
            if (index == -1) {
                index = children.size();
            }
            children.add(index + 1, previewNode);
        } else if (!visible && contains) {
            children.remove(previewNode);
        }
        previewNode.setVisible(visible);
    }

    public Node getPreview() {
        return previewNode;
    }
}
