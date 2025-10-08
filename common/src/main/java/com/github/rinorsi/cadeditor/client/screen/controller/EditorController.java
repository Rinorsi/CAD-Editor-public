package com.github.rinorsi.cadeditor.client.screen.controller;

import com.github.franckyi.guapi.api.mvc.Controller;
import com.github.rinorsi.cadeditor.client.screen.model.EditorModel;
import com.github.rinorsi.cadeditor.client.screen.view.ScreenView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.Component;

public interface EditorController<M extends EditorModel, V extends ScreenView> extends Controller<M, V> {
    @Override
    default void bind() {
        getModel().validProperty().addListener(this::updateDoneButton);
        getView().getSaveButton().setVisible(true);
        getView().getSaveButton().onAction(getModel()::save);
        getView().addCopyCommandButton(getModel().getContext().getCommandTooltip(), getModel().getContext().getCommandName());
        getView().getCopyCommandButton().setActive(getModel().getContext().isCopyCommand());
        getView().getCopyCommandButton().activeProperty().addListener(getModel().getContext()::setCopyCommand);
        getView().getCopyCommandButton().activeProperty().addListener(this::updateDoneButton);
        if (getModel().getContext().canSaveToVault()) {
            getView().addSaveVaultButton(getModel().getContext().getTargetName());
            getView().getSaveVaultButton().setActive(getModel().getContext().isSaveToVault());
            getView().getSaveVaultButton().activeProperty().addListener(getModel().getContext()::setSaveToVault);
            getView().getSaveVaultButton().activeProperty().addListener(this::updateDoneButton);
        }
        updateDoneButton();
    }

    default void updateDoneButton() {
        if (getModel().getContext().hasPermission()) {
            getView().getDoneButton().setDisable(!getModel().isValid());
            getView().getSaveButton().setDisable(!getModel().isValid());
            if (getModel().isValid()) {
                getView().getDoneButton().getTooltip().clear();
                getView().getSaveButton().getTooltip().clear();
            } else {
                getView().getDoneButton().getTooltip().setAll(ModTexts.FIX_ERRORS);
                getView().getSaveButton().getTooltip().setAll(ModTexts.FIX_ERRORS);
            }
        } else {
            boolean disable = true;
            Component label;
            Component tooltip = null;
            if (getModel().getContext().isSaveToVault() || getModel().getContext().isCopyCommand()) {
                label = getModel().getContext().isSaveToVault() ? ModTexts.SAVE_VAULT_GREEN : ModTexts.COPY_COMMAND_GREEN;
                if (getModel().isValid()) {
                    disable = false;
                } else {
                    tooltip = ModTexts.FIX_ERRORS;
                }
            } else {
                label = ModTexts.DONE;
                tooltip = getModel().getContext().getErrorTooltip();
            }
            getView().getDoneButton().setDisable(disable);
            getView().getDoneButton().setLabel(label);
            getView().getSaveButton().setDisable(true);
            if (tooltip == null) {
                getView().getSaveButton().getTooltip().clear();
            } else {
                getView().getSaveButton().getTooltip().setAll(tooltip);
            }
            if (tooltip == null) {
                getView().getDoneButton().getTooltip().clear();
            } else {
                getView().getDoneButton().getTooltip().setAll(tooltip);
            }
        }
    }
}
