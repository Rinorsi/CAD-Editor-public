
package com.github.rinorsi.cadeditor.client.screen.controller;

import com.github.franckyi.databindings.api.event.ObservableListChangeEvent;
import com.github.franckyi.databindings.api.event.ObservableListChangeListener;
import com.github.franckyi.guapi.api.Guapi;
import com.github.franckyi.guapi.api.mvc.AbstractController;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.UpdateLogScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.UpdateLogScreenModel.SectionLine;
import com.github.rinorsi.cadeditor.client.screen.view.UpdateLogScreenView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;

public class UpdateLogScreenController extends AbstractController<UpdateLogScreenModel, UpdateLogScreenView> {
    private static final int DETAIL_LIST_CELL_PADDING = 8;
    private final ObservableListChangeListener<SectionLine> lineListener = this::refreshDetailList;

    public UpdateLogScreenController(UpdateLogScreenModel model, UpdateLogScreenView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        // Keep header intro short; long CADE text is injected into the scroll list
        view.getIntroLabel().setLabel(com.github.franckyi.guapi.api.GuapiHelper.EMPTY_TEXT);
        view.getDoneButton().setLabel(ModTexts.CLOSE);
        view.getCancelButton().setVisible(false);

        view.getDoneButton().onAction(Guapi.getScreenHandler()::hideScene);
        view.getMarkReadButton().onAction(() -> {
            model.markSelectedAsRead();
            updateMarkReadState();
        });
        view.getMarkAllReadButton().onAction(() -> {
            model.markAllAsRead();
            updateMarkReadState();
        });
        view.getCopyButton().onAction(() -> {
            String payload = model.buildClipboardText();
            if (!payload.isEmpty()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(payload);
                ClientUtil.showMessage(ModTexts.Messages.successCopyClipboard(ModTexts.UPDATE_LOG.getString()));
            }
        });

        view.resetVersionIndicators();
        view.getVersionList().getItems().setAll(model.getVersions());
        model.getVersions().addListener(event -> {
            view.resetVersionIndicators();
            view.getVersionList().getItems().setAll(model.getVersions());
        });
        if (!model.getVersions().isEmpty()) {
            view.getVersionList().setFocusedElement(model.getVersions().get(0));
            model.select(model.getVersions().get(0));
        }
        view.getVersionList().focusedElementProperty().addListener((oldValue, newValue) -> {
            if (newValue != null) {
                model.select(newValue);
            }
        });

        model.selectedVersionProperty().addListener((oldValue, newValue) -> {
            view.renderEntry(newValue);
            updateMarkReadState();
        });

        refreshDetailList(null);
        model.getDisplayedLines().addListener(lineListener);
        updateMarkReadState();

        view.getDetailList().widthProperty().addListener((oldValue, newValue) -> updateWrapWidth(newValue));
        updateWrapWidth(view.getDetailList().getWidth());
    }

    private void refreshDetailList(ObservableListChangeEvent<? extends SectionLine> event) {
        view.getDetailList().getItems().setAll(model.getDisplayedLines());
    }

    private void updateWrapWidth(Integer width) {
        if (width == null) {
            return;
        }
        int padding = view.getDetailList().getPadding().getHorizontal();
        int available = Math.max(0, width - padding - DETAIL_LIST_CELL_PADDING);
        model.setWrapWidth(available);
    }

    private void updateMarkReadState() {
        boolean canMark = model.canMarkSelectedAsRead();
        view.getMarkReadButton().setDisable(!canMark);
        view.getMarkAllReadButton().setDisable(!model.canMarkAllAsRead());
        boolean showBadge = model.hasUnread() && model.getSelectedVersion() != null && model.getSelectedVersion().version().equals(model.getLatestVersion());
        view.setUnreadBadgeVisible(showBadge);
        view.updateUnreadState(model.hasUnread(), model.getLatestVersion());
    }
}
