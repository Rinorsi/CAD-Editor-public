package com.github.rinorsi.cadeditor.client.util.texteditor;

import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class TextEditorOutputFormatter {
    private final MutableComponent rootText;
    private int currentFormattingIndex;
    private int previousTextIndex;
    private List<Formatting> currentFormattings;

    public TextEditorOutputFormatter(MutableComponent rootText) {
        this.rootText = rootText;
    }

    public void format(String text, int firstCharacterIndex, List<Formatting> formattings) {
        initFormattingsForIndex(formattings, firstCharacterIndex);
        int currentTextIndex;
        for (currentTextIndex = 1; currentTextIndex <= text.length(); currentTextIndex++) {
            currentFormattingIndex++;
            List<Formatting> changedFormattings = getChangedFormattingsForCurrentIndex(formattings);
            if (changedFormattings.isEmpty()) {
                continue;
            }
            appendText(text.substring(previousTextIndex, currentTextIndex));
            changedFormattings.forEach(formatting -> {
                if (formatting.getStart() == currentFormattingIndex) {
                    currentFormattings.add(formatting);
                } else {
                    currentFormattings.remove(formatting);
                }
            });
            previousTextIndex = currentTextIndex;
        }
        if (previousTextIndex != currentTextIndex - 1) {
            appendText(text.substring(previousTextIndex, currentTextIndex - 1));
        }
    }

    private void initFormattingsForIndex(List<Formatting> formattings, int index) {
        currentFormattingIndex = index;
        currentFormattings = formattings.stream()
                .filter(formatting -> formatting.getStart() <= currentFormattingIndex && formatting.getEnd() > currentFormattingIndex)
                .collect(Collectors.toList());
    }

    private List<Formatting> getChangedFormattingsForCurrentIndex(List<Formatting> formattings) {
        return formattings.stream()
                .filter(formatting -> formatting.getStart() == currentFormattingIndex || formatting.getEnd() == currentFormattingIndex)
                .toList();
    }

    private void appendText(String s) {
        MutableComponent text = text(s);
        currentFormattings.forEach(formatting -> formatting.apply(text));
        rootText.append(text);
    }

    public MutableComponent getText() {
        return rootText;
    }
}
