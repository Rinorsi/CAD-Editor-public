package com.github.rinorsi.cadeditor.client.screen.view;

import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextArea;
import com.github.rinorsi.cadeditor.client.screen.widget.SyntaxHighlightingTextArea;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.franckyi.guapi.api.node.TexturedToggleButton;
import com.github.franckyi.guapi.api.node.TreeView;
import com.github.franckyi.guapi.api.node.VBox;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.screen.model.SNBTPreviewNode;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class SNBTEditorView extends ScreenView {
    private TextArea textArea;
    private TexturedButton formatButton;
    private TexturedToggleButton previewToggle;
    private VBox editorContainer;
    private VBox previewPane;
    private TreeView<SNBTPreviewNode> previewTree;
    private Label previewStatus;

    public SNBTEditorView() {
        super();
    }

    @Override
    protected MutableComponent getHeaderLabelText() {
        return ModTexts.editorTitle(ModTexts.gui("raw_data_text"));
    }

    @Override
    protected Node createButtonBar() {
        Node res = super.createButtonBar();
        buttonBarLeft.getChildren().addAll(hBox(2,
                formatButton = createButton(ModTextures.FORMAT, ModTexts.FORMAT),
                previewToggle = texturedToggleButton(ModTextures.LIST_TAG, 16, 16, false)
                        .tooltip(ModTexts.SNBT_PREVIEW_TOGGLE)
                        .action(this::togglePreview)
        ));
        return res;
    }

    @Override
    protected Node createEditor() {
        textArea = new SyntaxHighlightingTextArea()
                .minHeight(200)
                .prefHeight(Integer.MAX_VALUE)
                .maxHeight(Integer.MAX_VALUE);
        previewPane = vBox(preview -> {
            preview.spacing(4).fillWidth();
            preview.add(previewTree = treeView(SNBTPreviewNode.class)
                    .showRoot()
                    .itemHeight(18)
                    .childrenFocusable()
                    .padding(4)
                    .renderer(node -> label(text(node.getLabel()))), 1);
            preview.add(previewStatus = label(ModTexts.SNBT_PREVIEW_INVALID)
                    .textAlign(CENTER)
                    .visible(false)
                    .prefHeight(14));
        });
        return editorContainer = vBox(container -> {
            container.fillWidth();
            container.add(textArea, 1);
        });
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public TexturedButton getFormatButton() {
        return formatButton;
    }

    public TexturedToggleButton getPreviewToggle() {
        return previewToggle;
    }

    public TreeView<SNBTPreviewNode> getPreviewTree() {
        return previewTree;
    }

    public Label getPreviewStatus() {
        return previewStatus;
    }

    private void togglePreview() {
        boolean show = previewToggle.isActive();
        editorContainer.getChildren().clear();
        if (show) {
            editorContainer.getChildren().add(previewPane);
            editorContainer.setWeight(previewPane, 1);
        } else {
            editorContainer.getChildren().add(textArea);
            editorContainer.setWeight(textArea, 1);
        }
    }

    @Override
    public void build() {
        super.build();
        togglePreview();
    }

    public void refreshPreviewPane() {
        togglePreview();
    }
}
