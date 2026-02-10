package com.github.rinorsi.cadeditor.client.screen.view.selection.element;

import com.github.franckyi.guapi.api.mvc.View;
import com.github.franckyi.guapi.api.node.CheckBox;
import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Label;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class ListSelectionElementView implements View {
    private HBox root;
    private Label nameLabel;
    private Label idLabel;
    private CheckBox selectionCheckBox;

    @Override
    public void build() {
        root = hBox(root -> {
            root.add(vBox(labels -> {
                labels.add(nameLabel = label());
                labels.add(idLabel = label());
                labels.fillWidth().spacing(2).align(CENTER);
            }), 1);
            root.fillHeight().spacing(5).align(CENTER_LEFT);
        });
    }

    @Override
    public HBox getRoot() {
        return root;
    }

    public Label getNameLabel() {
        return nameLabel;
    }

    public Label getIdLabel() {
        return idLabel;
    }

    public void enableSelection() {
        if (selectionCheckBox == null) {
            selectionCheckBox = checkBox();
            root.getChildren().add(0, selectionCheckBox);
        }
    }

    public CheckBox getSelectionCheckBox() {
        return selectionCheckBox;
    }
}
