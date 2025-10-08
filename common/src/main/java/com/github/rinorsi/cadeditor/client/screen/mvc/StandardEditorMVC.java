package com.github.rinorsi.cadeditor.client.screen.mvc;

import com.github.franckyi.guapi.api.mvc.SimpleMVC;
import com.github.rinorsi.cadeditor.client.screen.controller.StandardEditorController;
import com.github.rinorsi.cadeditor.client.screen.model.StandardEditorModel;
import com.github.rinorsi.cadeditor.client.screen.view.StandardEditorView;

public final class StandardEditorMVC extends SimpleMVC<StandardEditorModel, StandardEditorView, StandardEditorController> {
    public static final StandardEditorMVC INSTANCE = new StandardEditorMVC();

    private StandardEditorMVC() {
        super(StandardEditorView::new, StandardEditorController::new);
    }
}
