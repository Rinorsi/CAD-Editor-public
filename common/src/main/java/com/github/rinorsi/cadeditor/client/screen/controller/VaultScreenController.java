package com.github.rinorsi.cadeditor.client.screen.controller;

import com.github.rinorsi.cadeditor.client.screen.model.VaultScreenModel;
import com.github.rinorsi.cadeditor.client.screen.view.VaultScreenView;
import com.github.rinorsi.cadeditor.common.ModTexts;

public class VaultScreenController extends CategoryEntryScreenController<VaultScreenModel, VaultScreenView> {
    public VaultScreenController(VaultScreenModel model, VaultScreenView view) {
        super(model, view);
        view.getHeaderLabel().setLabel(ModTexts.title(ModTexts.VAULT.copy()));
    }
}
