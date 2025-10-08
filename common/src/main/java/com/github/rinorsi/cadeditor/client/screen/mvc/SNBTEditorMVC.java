package com.github.rinorsi.cadeditor.client.screen.mvc;

import com.github.franckyi.guapi.api.mvc.SimpleMVC;
import com.github.rinorsi.cadeditor.client.screen.controller.SNBTEditorController;
import com.github.rinorsi.cadeditor.client.screen.model.SNBTEditorModel;
import com.github.rinorsi.cadeditor.client.screen.view.SNBTEditorView;

public final class SNBTEditorMVC extends SimpleMVC<SNBTEditorModel, SNBTEditorView, SNBTEditorController> {
    public static final SNBTEditorMVC INSTANCE = new SNBTEditorMVC();

    private SNBTEditorMVC() {
        super(SNBTEditorView::new, SNBTEditorController::new);
    }
}
