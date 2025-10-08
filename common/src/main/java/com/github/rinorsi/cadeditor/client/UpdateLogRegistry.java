package com.github.rinorsi.cadeditor.client;

import com.github.rinorsi.cadeditor.common.ModTexts;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class UpdateLogRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean loaded;
    private static List<UpdateLogEntry> entries = List.of();
    private static String latestVersion = "";

    private UpdateLogRegistry() { }

    public static void load() {
        if (loaded) return;

        List<UpdateLogEntry> versions = new ArrayList<>();
        String newest = "";

        JsonObject root = readJsonObject("/versions.json");
        if (root == null) root = readJsonObject("/assets/cadeditor/versions.json");

        if (root != null) {
            boolean first = true;
            for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                String version = e.getKey();
                JsonObject value = e.getValue().getAsJsonObject();
                String type = value.has("type") ? value.get("type").getAsString() : "";
                String date = value.has("date") ? value.get("date").getAsString() : "";
                String changelog = value.has("changelog") ? value.get("changelog").getAsString() : "";

                if ("CADE".equalsIgnoreCase(version)) {
                } else {
                    versions.add(parseEntry(version, type, date, changelog));
                }
                if (first) { newest = version; first = false; }
            }
        } else {
            LOGGER.warn("No versions.json found on classpath; showing CADE description only");
        }

        try {
            String cade = ModTexts.gui("cade_footer_long").getString();
            if (cade != null && !cade.isBlank() && versions.stream().noneMatch(v -> v.version().equals("CADE"))) {
                versions.add(0, new UpdateLogEntry("CADE", "", "", List.of()));
                if (newest.isEmpty()) newest = "CADE";
            }
        } catch (Throwable ignored) {}

        entries = List.copyOf(versions);
        latestVersion = newest;
        loaded = true;
    }

    private static JsonObject readJsonObject(String path) {
        try (InputStream stream = UpdateLogRegistry.class.getResourceAsStream(path)) {
            if (stream == null) return null;
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read {}", path, e);
            return null;
        }
    }

    private static UpdateLogEntry parseEntry(String version, String type, String date, String changelog) {
        Map<Section, List<MutableComponent>> sectionMap = new EnumMap<>(Section.class);
        for (Section section : Section.values()) sectionMap.put(section, new ArrayList<>());

        for (String line : parseChangelogLines(changelog)) {
            ParsedLine parsed = ParsedLine.fromRaw(line);
            sectionMap.get(parsed.section()).add(ModTexts.updateBullet(parsed.content()));
        }

        List<SectionContent> sections = new ArrayList<>();
        for (Section section : Section.values()) {
            List<MutableComponent> list = sectionMap.get(section);
            if (!list.isEmpty()) sections.add(new SectionContent(section, List.copyOf(list)));
        }
        if (sections.isEmpty()) sections.add(new SectionContent(Section.MISC, List.of(ModTexts.updateBullet(ModTexts.UPDATE_LOG_EMPTY.getString()))));
        return new UpdateLogEntry(version, type, date, List.copyOf(sections));
    }

    private static List<String> parseChangelogLines(String changelog) {
        if (changelog == null || changelog.isEmpty()) return List.of();
        String[] rawLines = changelog.split("\\r?\\n");
        List<String> lines = new ArrayList<>();
        for (String raw : rawLines) {
            String t = raw.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("-")) t = t.substring(1).trim();
            if (!t.isEmpty()) lines.add(t);
        }
        return lines;
    }

    public static List<UpdateLogEntry> getEntries() {
        if (!loaded) load();
        return entries;
    }

    public static String getLatestVersion() {
        if (!loaded) load();
        return latestVersion;
    }

    public enum Section {
        FEATURES("features", "新增"),
        IMPROVEMENTS("improvements", "体验", "优化", "调整"),
        FIXES("fixes", "修复"),
        KNOWN("known", "已知", "计划"),
        MISC("misc");

        private final String translationKey;
        private final String[] keywords;

        Section(String translationKey, String... keywords) {
            this.translationKey = translationKey;
            this.keywords = keywords;
        }

        public String translationKey() { return translationKey; }

        public static Section fromKeyword(String keyword) {
            if (keyword == null || keyword.isEmpty()) return MISC;
            String normalized = keyword.trim();
            for (Section s : values()) for (String k : s.keywords) if (k.equalsIgnoreCase(normalized)) return s;
            return MISC;
        }
    }

    public record SectionContent(Section section, List<MutableComponent> lines) { }

    public record UpdateLogEntry(String version, String type, String date, List<SectionContent> sections) {
        public MutableComponent displayName() {
            if (isProjectDescription()) {
                return ModTexts.updateLogAboutTitle();
            }
            return ModTexts.updateVersion(version, type);
        }

        public MutableComponent dateLabel() {
            if (isProjectDescription()) {
                return ModTexts.updateLogAboutSubtitle();
            }
            return (date == null || date.isBlank()) ? ModTexts.updateLogNoDate() : ModTexts.updateLogDate(date);
        }

        private boolean isProjectDescription() {
            return "CADE".equalsIgnoreCase(version) && (type == null || type.isBlank()) && (date == null || date.isBlank());
        }
    }

    private record ParsedLine(Section section, String content) {
        private static ParsedLine fromRaw(String line) {
            if (line.startsWith("[")) {
                int end = line.indexOf(']');
                if (end > 0) {
                    String keyword = line.substring(1, end);
                    String remainder = line.substring(end + 1).trim();
                    return new ParsedLine(Section.fromKeyword(keyword), remainder);
                }
            }
            return new ParsedLine(Section.MISC, line);
        }
    }
}

