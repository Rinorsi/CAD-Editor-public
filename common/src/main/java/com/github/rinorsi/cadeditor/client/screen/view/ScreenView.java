package com.github.rinorsi.cadeditor.client.screen.view;

import com.github.franckyi.guapi.api.mvc.View;
import com.github.franckyi.guapi.api.node.*;
import com.github.franckyi.guapi.api.node.builder.TexturedButtonBuilder;
import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.UpdateLogRegistry;
import com.github.rinorsi.cadeditor.client.util.ScreenScalingManager;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public abstract class ScreenView implements View {
    private VBox root;
    private Label headerLabel;
    private TexturedToggleButton copyCommandButton;
    private TexturedToggleButton saveVaultButton;
    private TexturedButton loadVaultButton;
    private TexturedButton openEditorButton;
    private TexturedButton openNBTEditorButton;
    private TexturedButton openSNBTEditorButton;
    private TexturedButton zoomResetButton;
    private TexturedButton zoomOutButton;
    private TexturedButton zoomInButton;
    private Button cancelButton;
    private Button saveButton;
    private Button doneButton;
    private Label zoomLabel;
    protected HBox buttonBar, buttonBarLeft, editorButtons, buttonBarCenter, buttonBarRight;

    @Override
    public void build() {
        root = vBox(root -> {
            root.spacing(5).align(CENTER).padding(5).fillWidth();
            root.add(createHeader());
            root.add(createMain(), 1);
            root.add(createFooter());
        });
        ScreenScalingManager.get().scaleProperty().addListener(this::onZoomUpdated);
        zoomResetButton.disableProperty().bind(ScreenScalingManager.get().canScaleBeResetProperty().not());
        onZoomUpdated();
    }

    protected Node createHeader() {
        return hBox(header -> {
            header.add(hBox().prefWidth(16));
            header.add(headerLabel = label(getHeaderLabelText()).textAlign(CENTER).prefHeight(20), 1);
            header.add(createButton(ModTextures.SETTINGS, ModTexts.SETTINGS).action(ModScreenHandler::openSettingsScreen));
            header.align(CENTER);
        });
    }

    protected MutableComponent getHeaderLabelText() {
        return EMPTY_TEXT;
    }

    protected Node createMain() {
        return vBox(main -> {
            main.add(createButtonBar());
            main.add(createEditor(), 1);
            main.spacing(2).fillWidth();
        });
    }

    protected Node createButtonBar() {
        return buttonBar = hBox(buttons -> {
            buttons.add(buttonBarLeft = hBox(editorButtons = hBox().spacing(2)).align(CENTER_LEFT).spacing(10));
            buttons.add(buttonBarCenter = hBox().align(CENTER).spacing(10), 1);
            buttons.add(buttonBarRight = hBox().align(CENTER_RIGHT).spacing(10));
            // Move Update Log button to top-right bar
            UpdateLogRegistry.load();
            var updateLogButton = createButton(ModTextures.UPDATE_LOG, ModTexts.UPDATE_LOG);
            if (ClientConfiguration.INSTANCE != null && !ClientConfiguration.INSTANCE.getLastSeenUpdateLogVersion().equals(UpdateLogRegistry.getLatestVersion())) {
                updateLogButton.tooltip(ModTexts.UPDATE_LOG_NEW_TOOLTIP);
            }
            buttonBarRight.getChildren().add(updateLogButton.action(ModScreenHandler::openUpdateLogScreen));
            buttonBarRight.getChildren().add(hBox(zoom -> {
                zoom.add(zoomResetButton = createButton(ModTextures.ZOOM_RESET, ModTexts.ZOOM_RESET).action(ScreenScalingManager.get()::restoreScale));
                zoom.add(zoomOutButton = createButton(ModTextures.ZOOM_OUT, ModTexts.ZOOM_OUT).action(ScreenScalingManager.get()::scaleDown));
                zoom.add(zoomLabel = label().prefWidth(25).textAlign(CENTER).padding(0, 3));
                zoom.add(zoomInButton = createButton(ModTextures.ZOOM_IN, ModTexts.ZOOM_IN).action(ScreenScalingManager.get()::scaleUp));
                zoom.spacing(2).align(CENTER);
            }));
            buttons.spacing(20).prefHeight(16);
        });
    }

    protected abstract Node createEditor();

    protected Node createFooter() {
        return vBox(box -> {
            box.spacing(4).align(CENTER);
            box.add(label(ModTexts.gui("cade_footer_short")).textAlign(CENTER).prefHeight(12));
            box.add(hBox(footer -> {
                footer.spacing(20).align(CENTER);
                footer.add(cancelButton = button(ModTexts.CANCEL).prefWidth(90));
                footer.add(saveButton = button(ModTexts.SAVE_EDIT).prefWidth(90).visible(false));
                footer.add(doneButton = button(ModTexts.DONE).prefWidth(90));
            }));
        });
    }

    protected TexturedButtonBuilder createButton(ResourceLocation id, String tooltipText) {
        return createButton(id, translated(tooltipText));
    }

    protected TexturedButtonBuilder createButton(ResourceLocation id, MutableComponent tooltipText) {
        return texturedButton(id, 16, 16, false).tooltip(tooltipText);
    }

    protected void onZoomUpdated() {
        zoomOutButton.setDisable(!ScreenScalingManager.get().canScaleDown());
        zoomLabel.setLabel(text(ScreenScalingManager.get().getScale() == 0 ? "Auto" : Integer.toString(ScreenScalingManager.get().getScale())));
        zoomInButton.setDisable(!ScreenScalingManager.get().canScaleUp());
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    public Label getHeaderLabel() {
        return headerLabel;
    }

    public TexturedToggleButton getCopyCommandButton() {
        return copyCommandButton;
    }

    public TexturedToggleButton getSaveVaultButton() {
        return saveVaultButton;
    }

    public TexturedButton getLoadVaultButton() {
        return loadVaultButton;
    }

    public TexturedButton getOpenEditorButton() {
        return openEditorButton;
    }

    public TexturedButton getOpenNBTEditorButton() {
        return openNBTEditorButton;
    }

    public TexturedButton getOpenSNBTEditorButton() {
        return openSNBTEditorButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getDoneButton() {
        return doneButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public void addSaveVaultButton(MutableComponent arg) {
        editorButtons.getChildren().add(saveVaultButton = texturedToggleButton(ModTextures.SAVE, 16, 16, false)
                .tooltip(ModTexts.SAVE_VAULT).action(() -> saveVaultButton.getTooltip().setAll(saveVaultButton.isActive() ?
                        Arrays.asList(ModTexts.savedVault(arg)) : Collections.singletonList(ModTexts.SAVE_VAULT))));
    }

    public void addLoadVaultButton(Runnable action) {
        editorButtons.getChildren().add(loadVaultButton = texturedButton(ModTextures.PASTE, 16, 16, false)
                .tooltip(ModTexts.LOAD_VAULT).action(action));
    }

    public void addOpenEditorButton(Runnable action) {
        editorButtons.getChildren().add(openEditorButton = texturedButton(ModTextures.EDITOR, 16, 16, false)
                .tooltip(ModTexts.OPEN_EDITOR).action(action));
    }

    public void addOpenNBTEditorButton(Runnable action) {
        editorButtons.getChildren().add(openNBTEditorButton = texturedButton(ModTextures.NBT_EDITOR, 16, 16, false)
                .tooltip(ModTexts.OPEN_NBT_EDITOR).action(action));
    }

    public void addOpenSNBTEditorButton(Runnable action) {
        editorButtons.getChildren().add(openSNBTEditorButton = texturedButton(ModTextures.SNBT_EDITOR, 16, 16, false)
                .tooltip(ModTexts.OPEN_SNBT_EDITOR).action(action));
    }

    public void addCopyCommandButton(MutableComponent copyText, String copiedText) {
        editorButtons.getChildren().add(copyCommandButton = texturedToggleButton(ModTextures.COPY_COMMAND, 16, 16, false)
                .tooltip(copyText).action(() -> copyCommandButton.getTooltip().setAll(copyCommandButton.isActive() ?
                        Arrays.asList(ModTexts.commandCopied(copiedText)) : Collections.singletonList(copyText))));
    }
}
