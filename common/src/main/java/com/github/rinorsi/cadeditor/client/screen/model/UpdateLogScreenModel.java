
package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.ObservableList;
import com.github.franckyi.guapi.api.mvc.Model;
import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import com.github.rinorsi.cadeditor.client.UpdateLogRegistry;
import com.github.rinorsi.cadeditor.client.UpdateLogRegistry.SectionContent;
import com.github.rinorsi.cadeditor.client.UpdateLogRegistry.UpdateLogEntry;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

import static com.github.franckyi.guapi.api.GuapiHelper.EMPTY_TEXT;
import static com.github.franckyi.guapi.api.GuapiHelper.text;

public final class UpdateLogScreenModel implements Model {
    private static final String BULLET_PREFIX = "• ";
    private static final String CONTINUATION_PREFIX = "  ";
    private static final int MIN_CONTENT_WIDTH = 40;

    private int wrapWidth = 240;

    private final ObservableList<UpdateLogEntry> versions = ObservableList.create();
    private final ObjectProperty<UpdateLogEntry> selectedVersion = ObjectProperty.create();
    private final ObservableList<SectionLine> displayedLines = ObservableList.create();

    public UpdateLogScreenModel() {
        UpdateLogRegistry.load();
        versions.setAll(UpdateLogRegistry.getEntries());
        if (!versions.isEmpty()) {
            selectedVersion.setValue(versions.get(0));
        }
        selectedVersion.addListener((oldValue, newValue) -> rebuildLines(newValue));
        rebuildLines(selectedVersion.getValue());
    }

    public ObservableList<UpdateLogEntry> getVersions() {
        return versions;
    }

    public ObjectProperty<UpdateLogEntry> selectedVersionProperty() {
        return selectedVersion;
    }

    public ObservableList<SectionLine> getDisplayedLines() {
        return displayedLines;
    }

    public UpdateLogEntry getSelectedVersion() {
        return selectedVersion.getValue();
    }

    public void select(UpdateLogEntry entry) {
        selectedVersion.setValue(entry);
    }

    public boolean hasUnread() {
        return !ClientConfiguration.INSTANCE.getLastSeenUpdateLogVersion().equals(UpdateLogRegistry.getLatestVersion());
    }

    public boolean canMarkSelectedAsRead() {
        UpdateLogEntry entry = getSelectedVersion();
        return entry != null && !entry.version().equals(ClientConfiguration.INSTANCE.getLastSeenUpdateLogVersion());
    }

    public boolean canMarkAllAsRead() {
        return hasUnread();
    }

    public void markSelectedAsRead() {
        UpdateLogEntry entry = getSelectedVersion();
        if (entry != null) {
            ClientConfiguration.INSTANCE.setLastSeenUpdateLogVersion(entry.version());
            ClientConfiguration.save();
        }
    }

    public void markAllAsRead() {
        String latest = getLatestVersion();
        if (!latest.isEmpty()) {
            ClientConfiguration.INSTANCE.setLastSeenUpdateLogVersion(latest);
            ClientConfiguration.save();
        }
    }

    public void setWrapWidth(int width) {
        if (width <= 0) {
            return;
        }
        int clamped = Math.max(MIN_CONTENT_WIDTH, width);
        if (clamped != wrapWidth) {
            wrapWidth = clamped;
            rebuildLines(selectedVersion.getValue());
        }
    }

    public String getLatestVersion() {
        return UpdateLogRegistry.getLatestVersion();
    }

    public String buildClipboardText() {
        UpdateLogEntry entry = getSelectedVersion();
        if (entry == null) {
            return "";
        }
        String ls = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append(entry.displayName().getString()).append(ls);
        builder.append(entry.dateLabel().getString()).append(ls);
        for (SectionLine line : displayedLines) {
            String lineText = line.text().getString();
            if (!lineText.isEmpty()) {
                builder.append(lineText).append(ls);
            }
        }
        return builder.toString().trim();
    }

    private void rebuildLines(UpdateLogEntry entry) {
        if (entry == null) {
            displayedLines.clear();
            return;
        }
        List<SectionLine> lines = new ArrayList<>();
        if ("CADE".equalsIgnoreCase(entry.version())) {
            String intro = ModTexts.gui("cade_footer_long").getString();
            if (!intro.isEmpty()) {
                for (String part : intro.split("\\r?\\n")) {
                    for (String wrapped : splitPlainText(part, wrapWidth)) {
                        if (!wrapped.isEmpty()) {
                            lines.add(SectionLine.item(text(wrapped)));
                        }
                    }
                }
            }
        } else for (SectionContent content : entry.sections()) {
            lines.add(SectionLine.header(ModTexts.updateLogSectionTitle(content.section().translationKey())));
            for (MutableComponent bullet : content.lines()) {
                lines.addAll(wrapBulletLine(bullet));
            }
            lines.add(SectionLine.spacer());
        }
        if (!lines.isEmpty() && lines.get(lines.size() - 1).type() == SectionLine.Type.SPACER) {
            lines.remove(lines.size() - 1);
        }
        displayedLines.setAll(lines);
    }

    public record SectionLine(Type type, MutableComponent text) {
        public static SectionLine header(MutableComponent text) {
            return new SectionLine(Type.HEADER, text);
        }

        public static SectionLine item(MutableComponent text) {
            return new SectionLine(Type.ITEM, text);
        }

        public static SectionLine spacer() {
            return new SectionLine(Type.SPACER, EMPTY_TEXT);
        }

        public enum Type {
            HEADER,
            ITEM,
            SPACER
        }
    }

    private List<SectionLine> wrapBulletLine(MutableComponent bullet) {
        List<SectionLine> out = new ArrayList<>();
        if (bullet == null) {
            return out;
        }
        String raw = bullet.getString();
        if (raw == null) {
            return out;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            out.add(SectionLine.item(bullet));
            return out;
        }
        boolean hasBullet = trimmed.startsWith("•");
        String content = hasBullet ? trimmed.substring(1).trim() : trimmed;
        Font font = Minecraft.getInstance().font;
        int prefixWidth = Math.max(font.width(BULLET_PREFIX), font.width(CONTINUATION_PREFIX));
        int available = Math.max(MIN_CONTENT_WIDTH, wrapWidth - prefixWidth);
        List<String> wrapped = splitPlainText(content, available);
        if (wrapped.isEmpty()) {
            out.add(SectionLine.item(bullet));
            return out;
        }
        if (hasBullet) {
            out.add(SectionLine.item(ModTexts.updateBullet(wrapped.get(0))));
        } else {
            out.add(SectionLine.item(text(wrapped.get(0)).withStyle(ChatFormatting.GRAY)));
        }
        for (int i = 1; i < wrapped.size(); i++) {
            out.add(SectionLine.item(text(CONTINUATION_PREFIX + wrapped.get(i)).withStyle(ChatFormatting.GRAY)));
        }
        return out;
    }

    private List<String> splitPlainText(String text, int width) {
        List<String> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        Font font = Minecraft.getInstance().font;
        int effectiveWidth = Math.max(MIN_CONTENT_WIDTH, width);
        Component literal = Component.literal(text);
        for (FormattedCharSequence sequence : font.split(literal, effectiveWidth)) {
            String sanitized = sequenceToString(sequence).strip();
            if (!sanitized.isEmpty()) {
                out.add(sanitized);
            }
        }
        return out;
    }

    private static String sequenceToString(FormattedCharSequence sequence) {
        StringBuilder builder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }
}

