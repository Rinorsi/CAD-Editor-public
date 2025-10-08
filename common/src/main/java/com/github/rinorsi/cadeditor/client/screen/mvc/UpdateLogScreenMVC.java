package com.github.rinorsi.cadeditor.client.screen.mvc;

import com.github.franckyi.guapi.api.mvc.SimpleMVC;
import com.github.rinorsi.cadeditor.client.screen.controller.UpdateLogScreenController;
import com.github.rinorsi.cadeditor.client.screen.model.UpdateLogScreenModel;
import com.github.rinorsi.cadeditor.client.screen.view.UpdateLogScreenView;

public final class UpdateLogScreenMVC extends SimpleMVC<UpdateLogScreenModel, UpdateLogScreenView, UpdateLogScreenController> {
    public static final UpdateLogScreenMVC INSTANCE = new UpdateLogScreenMVC();

    private UpdateLogScreenMVC() {
        super(UpdateLogScreenView::new, UpdateLogScreenController::new);
    }
}
