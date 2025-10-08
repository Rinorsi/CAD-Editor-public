package com.github.rinorsi.cadeditor.client.screen.view.entry;

import com.github.franckyi.guapi.api.node.CheckBox;
import com.github.franckyi.guapi.api.node.Node;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class BooleanEntryView extends LabeledEntryView {
    private CheckBox checkBox;

    @Override
    protected Node createLabeledContent() {
        return hBox(checkBox = checkBox());
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }
}
