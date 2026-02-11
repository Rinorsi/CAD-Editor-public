package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.rinorsi.cadeditor.client.screen.model.entry.LabeledEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.LabeledEntryView;

public abstract class LabeledEntryController<M extends LabeledEntryModel, V extends LabeledEntryView> extends EntryController<M, V> {
    public LabeledEntryController(M model, V view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getLabel().labelProperty().bind(model.labelProperty());
        syncLabelTooltip();
        model.labelTooltipProperty().addListener(this::syncLabelTooltip);
        if (view.getLabel().getParent() == view.getRoot()) {
            int labelWeight = model.getLabelWeight();
            if (labelWeight <= 0) {
                view.getRoot().getChildren().remove(view.getLabel());
            } else {
                view.getRoot().setWeight(view.getLabel(), labelWeight);
            }
        }
    }

    private void syncLabelTooltip() {
        List<Component> lines = new ArrayList<>(model.getLabelTooltip());
        if (lines.isEmpty()) {
            lines.addAll(resolveWikiTooltipFromLabel());
        }
        view.getLabel().getTooltip().setAll(wrapTooltipLines(lines, 28));
    }

    private Collection<Component> resolveWikiTooltipFromLabel() {
        if (model.getLabel() == null) {
            return List.of();
        }
        if (!(model.getLabel().getContents() instanceof TranslatableContents contents)) {
            return List.of();
        }
        String key = contents.getKey();
        String prefix = "cadeditor.gui.";
        if (!key.startsWith(prefix)) {
            return List.of();
        }
        String suffix = key.substring(prefix.length());
        if (suffix.startsWith("wiki.")) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        String base = "cadeditor.gui.wiki." + suffix;
        if (I18n.exists(base)) {
            lines.add(Component.translatable(base).withStyle(ChatFormatting.GRAY));
        }
        for (int i = 0; i < 16; i++) {
            String indexed = base + "." + i;
            if (!I18n.exists(indexed)) {
                if (i == 0 && lines.isEmpty()) {
                    return List.of();
                }
                break;
            }
            lines.add(Component.translatable(indexed).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    private List<Component> wrapTooltipLines(Collection<Component> source, int maxChars) {
        List<Component> out = new ArrayList<>();
        for (Component component : source) {
            if (component == null) {
                continue;
            }
            String text = component.getString();
            if (text == null || text.isBlank()) {
                continue;
            }
            String[] chunks = text.split("\\\\n|\\n");
            for (String chunk : chunks) {
                appendWrapped(out, chunk.trim(), component, maxChars);
            }
        }
        return out;
    }

    private void appendWrapped(List<Component> out, String text, Component source, int maxChars) {
        if (text.isEmpty()) {
            return;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + maxChars);
            if (end < text.length()) {
                int split = lastBreak(text, start, end);
                if (split > start + 6) {
                    end = split + 1;
                }
            }
            String piece = text.substring(start, end).trim();
            if (!piece.isEmpty()) {
                out.add(Component.literal(piece).withStyle(source.getStyle()));
            }
            start = end;
        }
    }

    private int lastBreak(String text, int start, int endExclusive) {
        for (int i = endExclusive - 1; i > start; i--) {
            char c = text.charAt(i);
            if (c == ' ' || c == ',' || c == '.' || c == ';' || c == ':' || c == '，' || c == '。' || c == '；' || c == '：' || c == '、' || c == ')' || c == '）') {
                return i;
            }
        }
        return -1;
    }
}
