package com.github.rinorsi.cadeditor.client.screen.view.entry;

public class TextEntryView extends TextFieldEntryView {
    @Override
    public void build() {
        super.build();
        getTextField().setValidator(s -> true);
    }
}
