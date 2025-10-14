package com.github.rinorsi.cadeditor.client.screen.view.entry.item;

import com.github.franckyi.guapi.api.node.Button;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextArea;
import com.github.franckyi.guapi.api.node.VBox;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.Component;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class WritableBookPagesEntryView extends EntryView {
    private Button prevButton;
    private Button nextButton;
    private Button addButton;
    private Button removeButton;
    private Label statusLabel;
    private TextArea editorArea;

    @Override
    public void build() {
        super.build();
        getButtonBox().getChildren().clear();
        getButtonBox().getChildren().add(createButtonColumn());
    }

    @Override
    protected Node createContent() {
        return vBox(content -> {
            content.add(editorArea = textArea()
                    .prefHeight(160)
                    .prefWidth(260)
                    .maxWidth(Integer.MAX_VALUE)
                    .validator(s -> true));
            content.add(statusLabel = label(Component.empty()));
            content.spacing(4).fillWidth();
        });
    }

    private VBox createButtonColumn() {
        return vBox(column -> {
            column.add(prevButton = button(ModTexts.gui("book_page_prev"))
                    .prefWidth(120));
            column.add(nextButton = button(ModTexts.gui("book_page_next"))
                    .prefWidth(120));
            column.add(addButton = button(ModTexts.gui("book_page_add"))
                    .prefWidth(120));
            column.add(removeButton = button(ModTexts.gui("book_page_remove"))
                    .prefWidth(120));
            column.spacing(4).align(TOP_RIGHT).fillWidth();
        });
    }

    public Button getPrevButton() { return prevButton; }

    public Button getNextButton() { return nextButton; }

    public Button getAddButton() { return addButton; }

    public Button getRemoveButton() { return removeButton; }

    public Label getStatusLabel() { return statusLabel; }

    public TextArea getEditorArea() { return editorArea; }
}
