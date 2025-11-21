package com.github.rinorsi.cadeditor.client.screen.controller;

import com.github.franckyi.guapi.api.Guapi;
import com.github.franckyi.guapi.api.mvc.AbstractController;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.SNBTEditorModel;
import com.github.rinorsi.cadeditor.client.screen.view.SNBTEditorView;
import com.github.rinorsi.cadeditor.client.screen.widget.SyntaxHighlightingTextArea;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.TagParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Objects;

public class SNBTEditorController extends AbstractController<SNBTEditorModel, SNBTEditorView> implements EditorController<SNBTEditorModel, SNBTEditorView> {

    private static final Logger LOGGER = LogManager.getLogger();

    public SNBTEditorController(SNBTEditorModel model, SNBTEditorView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        EditorController.super.bind();
        view.addOpenEditorButton(() -> runWithParseGuard(() -> model.changeEditor(EditorType.STANDARD)));
        view.addOpenNBTEditorButton(() -> runWithParseGuard(() -> model.changeEditor(EditorType.NBT)));
        view.getTextArea().textProperty().bindBidirectional(model.valueProperty());
        view.getTextArea().setValidator(s -> {
            try {
                return TagParser.parseTag(s) != null;
            } catch (CommandSyntaxException e) {
                return false;
            }
        });
        view.getPreviewTree().rootItemProperty().bind(model.previewRootProperty());
        view.getPreviewTree().visibleProperty().bind(model.validProperty());
        view.getPreviewStatus().visibleProperty().bind(model.validProperty().not());
        view.getPreviewToggle().disableProperty().bind(model.previewRootProperty().mapToBoolean(Objects::isNull));
        model.previewRootProperty().addListener(root -> {
            if (root == null && view.getPreviewToggle().isActive()) {
                view.getPreviewToggle().setActive(false);
                view.refreshPreviewPane();
            }
        });
        view.getFormatButton().disableProperty().bind(view.getTextArea().validProperty().not());
        view.getFormatButton().onAction(this::format);
        SyntaxHighlightingTextArea textArea = (SyntaxHighlightingTextArea) view.getTextArea();
        model.parseErrorIndexProperty().addListener(textArea::setParseErrorIndex);
        textArea.setParseErrorIndex(model.parseErrorIndexProperty().getValue());
        view.getDoneButton().onAction(() -> runWithParseGuard(model::update));
        view.getCancelButton().onAction(Guapi.getScreenHandler()::hideScene);
    }

    private void format() {
        SnbtPrinterTagVisitor formatter = new SnbtPrinterTagVisitor("  ", 0, new ArrayList<>());
        try {
            model.setValue(formatter.visit(TagParser.parseTag(view.getTextArea().getText())));
            model.parseErrorIndexProperty().setValue(null);
        } catch (CommandSyntaxException e) {
            int cursor = Math.max(0, Math.min(view.getTextArea().getText().length(), e.getCursor()));
            model.parseErrorIndexProperty().setValue(cursor);
            notifyParseFailure(e);
        }
    }

    private void runWithParseGuard(Runnable action) {
        try {
            action.run();
        } catch (SNBTEditorModel.ParseException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CommandSyntaxException syntaxException) {
                notifyParseFailure(syntaxException);
            } else {
                notifyParseFailure(e);
            }
        }
    }

    private void notifyParseFailure(Exception e) {
        LOGGER.error("SNBT parse failed", e);
        ClientUtil.showMessage(ModTexts.Messages.SNBT_PARSE_ERROR);
    }
}


