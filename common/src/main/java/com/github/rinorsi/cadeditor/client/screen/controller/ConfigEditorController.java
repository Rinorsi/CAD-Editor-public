package com.github.rinorsi.cadeditor.client.screen.controller;

import com.github.rinorsi.cadeditor.client.screen.model.ConfigEditorScreenModel;
import com.github.rinorsi.cadeditor.client.screen.view.ConfigScreenView;

public class ConfigEditorController extends CategoryEntryScreenController<ConfigEditorScreenModel, ConfigScreenView> {
    public ConfigEditorController(ConfigEditorScreenModel model, ConfigScreenView view) {
        super(model, view);
    }
}
