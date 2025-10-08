package com.github.rinorsi.cadeditor.client.screen.view.entry;

import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public abstract class TextFieldEntryView extends LabeledEntryView {
    private TextField textField;

    @Override
    protected Node createLabeledContent() {
        return textField = textField().prefHeight(16);
    }

    public TextField getTextField() {
        return textField;
    }
}
