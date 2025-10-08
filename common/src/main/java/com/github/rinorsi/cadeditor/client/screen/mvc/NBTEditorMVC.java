package com.github.rinorsi.cadeditor.client.screen.mvc;

import com.github.franckyi.guapi.api.mvc.SimpleMVC;
import com.github.rinorsi.cadeditor.client.screen.controller.NBTEditorController;
import com.github.rinorsi.cadeditor.client.screen.model.NBTEditorModel;
import com.github.rinorsi.cadeditor.client.screen.view.NBTEditorView;

public final class NBTEditorMVC extends SimpleMVC<NBTEditorModel, NBTEditorView, NBTEditorController> {
    public static final NBTEditorMVC INSTANCE = new NBTEditorMVC();

    private NBTEditorMVC() {
        super(NBTEditorView::new, NBTEditorController::new);
    }
}
