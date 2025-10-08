package com.github.rinorsi.cadeditor.client.screen.mvc;

import com.github.franckyi.guapi.api.mvc.SimpleMVC;
import com.github.rinorsi.cadeditor.client.screen.controller.ConfigEditorController;
import com.github.rinorsi.cadeditor.client.screen.model.ConfigEditorScreenModel;
import com.github.rinorsi.cadeditor.client.screen.view.ConfigScreenView;

public final class ConfigEditorMVC extends SimpleMVC<ConfigEditorScreenModel, ConfigScreenView, ConfigEditorController> {
    public static final ConfigEditorMVC INSTANCE = new ConfigEditorMVC();

    private ConfigEditorMVC() {
        super(ConfigScreenView::new, ConfigEditorController::new);
    }
}
