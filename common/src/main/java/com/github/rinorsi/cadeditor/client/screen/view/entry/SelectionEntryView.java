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
    private Node textContent;

    @Override
    protected Node createLabeledContent() {
        textContent = super.createLabeledContent();
        return selectionBox = hBox(box -> {
            box.add(textContent, 1);
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
            children.add(0, previewNode);
        } else if (!visible && contains) {
            children.remove(previewNode);
        }
        previewNode.setVisible(visible);
    }

    public Node getPreview() {
        return previewNode;
    }
}
