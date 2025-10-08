package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.franckyi.databindings.api.ObservableList;
import com.github.rinorsi.cadeditor.client.screen.model.entry.TextEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.TextEntryView;
import com.github.rinorsi.cadeditor.client.util.texteditor.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class TextEntryController extends ValueEntryController<TextEntryModel, TextEntryView> implements TextEditorActionHandler {
    private final ObservableList<Formatting> formattings = ObservableList.create();
    private boolean isResettingModel;

    public TextEntryController(TextEntryModel model, TextEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getTextField().setText(model.getValue().getString());
        view.getTextField().setTextRenderer(this::renderText);
        view.getTextField().focusedProperty().addListener(this::onTextFieldFocus);
        view.getTextField().setOnTextUpdate(this::onTextUpdate);
        view.getTextField().textProperty().addListener(this::updateModel);
        formattings.addListener(this::updateModel);
        view.getTextField().validProperty().addListener(model::setValid);
        initFormattings(model.getValue());
        model.resetDefaultValue();
        model.setOnApply(this::updateModel);
    }

    private void updateModel() {
        if (!isResettingModel) {
            model.setValue(createText());
        }
    }

    @Override
    protected void resetModel() {
        super.resetModel();
        isResettingModel = true;
        view.getTextField().setText(model.getValue().getString());
        initFormattings(model.getValue());
        isResettingModel = false;
    }

    private void onTextUpdate(int oldCursorPos, int oldHighlightPos, String oldText, int newCursorPos, String newText) {
        int oldLength = oldText.length();
        int newLength = newText.length();
        int amount = newLength - oldLength;
        if (amount != 0) {
            if (oldCursorPos == oldHighlightPos && oldCursorPos != newCursorPos) {
                formattings.removeIf(formatting -> {
                    if (oldCursorPos <= formatting.getStart()) {
                        formatting.setStart(formatting.getStart() + amount);
                        formatting.setEnd(formatting.getEnd() + amount);
                    } else if (oldCursorPos > formatting.getStart() && oldCursorPos <= formatting.getEnd()) {
                        formatting.setEnd(formatting.getEnd() + amount);
                    }
                    return formatting.getStart() >= formatting.getEnd();
                });
            } else {
                int pos = Math.min(oldCursorPos, oldHighlightPos);
                formattings.removeIf(formatting -> {
                    if (pos < formatting.getStart()) {
                        formatting.setStart(formatting.getStart() + amount);
                        formatting.setEnd(formatting.getEnd() + amount);
                    } else if (pos >= formatting.getStart() && pos <= formatting.getEnd()) {
                        formatting.setEnd(formatting.getEnd() + amount);
                    }
                    return formatting.getStart() >= formatting.getEnd();
                });
            }
        }
    }

    private Component renderText(String str, int firstCharacterIndex) {
        if (!str.isEmpty()) {
            TextEditorOutputFormatter formatter = new TextEditorOutputFormatter(text().append(text("")));
            formatter.format(str, firstCharacterIndex, formattings);
            return formatter.getText();
        }
        return EMPTY_TEXT;
    }

    private MutableComponent createText() {
        TextEditorOutputFormatter formatter = new TextEditorOutputFormatter(text().append(text("")));
        formatter.format(view.getTextField().getText(), 0, formattings);
        return formatter.getText();
    }

    private void initFormattings(MutableComponent text) {
        TextEditorInputParser parser = new TextEditorInputParser();
        parser.parse(text);
        formattings.setAll(parser.getFormattings());
    }

    private void onTextFieldFocus(boolean focused) {
        if (focused) {
            model.getCategory().getParent().setActiveTextEditor(this);
        } else if (model.getCategory().getParent().getActiveTextEditor() == this) {
            model.getCategory().getParent().setActiveTextEditor(null);
        }
    }

    @Override
    public void removeColorFormatting() {
        int i = view.getTextField().getCursorPosition();
        int j = view.getTextField().getHighlightPosition();
        if (i == j) return;
        resizeOtherColorFormattings(new ColorFormatting(Math.min(i, j), Math.max(i, j), null), false);
    }

    @Override
    public void addColorFormatting(String color) {
        int i = view.getTextField().getCursorPosition();
        int j = view.getTextField().getHighlightPosition();
        if (i == j) return;
        ColorFormatting formatting = new ColorFormatting(Math.min(i, j), Math.max(i, j), color);
        if (formattings.contains(formatting)) {
            return;
        }
        mergeIdenticalFormattings(ColorFormatting.class, other -> other.getColor().equals(color), formatting);
        resizeOtherColorFormattings(formatting, true);
    }

    @Override
    public void addStyleFormatting(StyleType target) {
        int i = view.getTextField().getCursorPosition();
        int j = view.getTextField().getHighlightPosition();
        if (i == j) return;
        StyleFormatting formatting = new StyleFormatting(Math.min(i, j), Math.max(i, j), target);
        if (formattings.contains(formatting)) {
            formattings.remove(formatting);
            return;
        }
        Optional<StyleFormatting> surrounding = getSurroundingStyleFormatting(formatting);
        if (surrounding.isPresent()) {
            removeStyleFormatting(formatting, surrounding.get());
        } else {
            mergeIdenticalFormattings(StyleFormatting.class, other -> other.getType().equals(target), formatting);
            formattings.add(formatting);
        }
    }

    private <T extends Formatting> void mergeIdenticalFormattings(Class<T> formattingClass, Predicate<T> identicalPredicate, T formatting) {
        Iterator<Formatting> it = formattings.iterator();
        while (it.hasNext()) {
            Formatting f = it.next();
            if (formattingClass.isInstance(f)) {
                T other = formattingClass.cast(f);
                if (identicalPredicate.test(other)) {
                    boolean remove = false;
                    if (other.getEnd() >= formatting.getStart() && other.getEnd() <= formatting.getEnd()) {
                        remove = true;
                        if (other.getStart() < formatting.getStart()) {
                            formatting.setStart(other.getStart());
                        }
                    }
                    if (other.getStart() >= formatting.getStart() && other.getStart() <= formatting.getEnd()) {
                        remove = true;
                        if (other.getEnd() > formatting.getEnd()) {
                            formatting.setEnd(other.getEnd());
                        }
                    }
                    if (remove) {
                        it.remove();
                    }
                }
            }
        }
    }

    private void resizeOtherColorFormattings(ColorFormatting formatting, boolean add) {
        List<Formatting> addedFormattings = new ArrayList<>();
        if (add) {
            addedFormattings.add(formatting);
        }
        Iterator<Formatting> it = formattings.iterator();
        while (it.hasNext()) {
            Formatting f = it.next();
            if (f instanceof ColorFormatting other) {
                if (!other.getColor().equals(formatting.getColor())) {
                    if (formatting.getStart() <= other.getStart() && formatting.getEnd() >= other.getEnd()) {
                        it.remove();
                        continue;
                    }
                    if (formatting.getStart() <= other.getStart() && formatting.getEnd() > other.getStart() && formatting.getEnd() <= other.getEnd()) {
                        other.setStart(formatting.getEnd());
                    }
                    if (formatting.getEnd() >= other.getEnd() && formatting.getStart() < other.getEnd() && formatting.getStart() >= other.getStart()) {
                        other.setEnd(formatting.getStart());
                    }
                    if (formatting.getStart() >= other.getStart() && formatting.getEnd() > other.getStart() && formatting.getEnd() <= other.getEnd()) {
                        addedFormattings.add(new ColorFormatting(formatting.getEnd(), other.getEnd(), other.getColor()));
                        other.setEnd(formatting.getStart());
                    }
                    if (other.getStart() >= other.getEnd()) {
                        it.remove();
                    }
                }
            }
        }
        formattings.addAll(addedFormattings);
    }

    private Optional<StyleFormatting> getSurroundingStyleFormatting(StyleFormatting formatting) {
        return formattings.stream()
                .filter(StyleFormatting.class::isInstance)
                .map(StyleFormatting.class::cast)
                .filter(other -> other.getType().equals(formatting.getType()))
                .filter(other -> formatting.getStart() >= other.getStart() && formatting.getEnd() <= other.getEnd())
                .findFirst();
    }

    private void removeStyleFormatting(StyleFormatting formatting, StyleFormatting other) {
        if (formatting.getStart() == other.getStart()) {
            other.setStart(formatting.getEnd());
        } else if (formatting.getEnd() == other.getEnd()) {
            other.setEnd(formatting.getStart());
        } else {
            int otherEnd = other.getEnd();
            other.setEnd(formatting.getStart());
            formatting.setStart(formatting.getEnd());
            formatting.setEnd(otherEnd);
            formattings.add(formatting);
        }
    }
}
