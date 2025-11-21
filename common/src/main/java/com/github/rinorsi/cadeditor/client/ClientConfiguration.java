package com.github.rinorsi.cadeditor.client;

import com.github.franckyi.guapi.api.util.DebugMode;
import com.github.rinorsi.cadeditor.PlatformUtil;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.client.util.texteditor.SyntaxHighlightingPreset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path OLD_CLIENT_CONFIG_FILE = PlatformUtil.getConfigDir().resolve("ibeeditor-client.json");
    private static final Path CLIENT_CONFIG_FILE = PlatformUtil.getConfigDir().resolve("cadeditor-client.json");
    public static ClientConfiguration INSTANCE;
    private static boolean changed;

    private final int version;
    private int editorScale;
    private String guapiTheme;
    private DebugMode guapiDebugMode;
    private int selectionScreenMaxItems;
    private String lastSeenUpdateLogVersion;
    private String syntaxHighlightingPreset;
    private int snbtLineSpacing;
    private int snbtSelectionBackgroundColor;

    private ClientConfiguration() {
        version = 0;
        editorScale = -1;
        guapiTheme = "vanilla";
        guapiDebugMode = DebugMode.NONE;
        selectionScreenMaxItems = 100;
        lastSeenUpdateLogVersion = "";
        syntaxHighlightingPreset = SyntaxHighlightingPreset.LEGACY.id();
        snbtLineSpacing = 2;
        snbtSelectionBackgroundColor = 0x66FFFFFF;
    }

    public int getEditorScale() {
        return editorScale;
    }

    public void setEditorScale(int editorScale) {
        if (this.editorScale != editorScale) {
            this.editorScale = editorScale;
            changed = true;
        }
    }

    public String getGuapiTheme() {
        return guapiTheme;
    }

    public void setGuapiTheme(String guapiTheme) {
        if (!this.guapiTheme.equals(guapiTheme)) {
            this.guapiTheme = guapiTheme;
            changed = true;
        }
        //TODO 主题要继续扩展（排期靠后，未来可期）
    }

    public DebugMode getGuapiDebugMode() {
        return guapiDebugMode == null ? DebugMode.NONE : guapiDebugMode;
    }

    public void setGuapiDebugMode(DebugMode guapiDebugMode) {
        DebugMode sanitized = guapiDebugMode == null ? DebugMode.NONE : guapiDebugMode;
        if (this.guapiDebugMode != sanitized) {
            this.guapiDebugMode = sanitized;
            changed = true;
            DebugLog.modeChanged(sanitized);
        }
    }

    public int getSelectionScreenMaxItems() {
        return selectionScreenMaxItems;
    }

    public void setSelectionScreenMaxItems(int selectionScreenMaxItems) {
        if (this.selectionScreenMaxItems != selectionScreenMaxItems) {
            this.selectionScreenMaxItems = selectionScreenMaxItems;
            changed = true;
        }
    }

    public String getLastSeenUpdateLogVersion() {
        return lastSeenUpdateLogVersion;
    }

    public void setLastSeenUpdateLogVersion(String lastSeenUpdateLogVersion) {
        String sanitized = lastSeenUpdateLogVersion == null ? "" : lastSeenUpdateLogVersion;
        if (!sanitized.equals(this.lastSeenUpdateLogVersion)) {
            this.lastSeenUpdateLogVersion = sanitized;
            changed = true;
        }
    }

    public SyntaxHighlightingPreset getSyntaxHighlightingPreset() {
        return SyntaxHighlightingPreset.byId(syntaxHighlightingPreset);
    }

    public void setSyntaxHighlightingPreset(SyntaxHighlightingPreset preset) {
        SyntaxHighlightingPreset resolved = preset == null ? SyntaxHighlightingPreset.LEGACY : preset;
        if (!resolved.id().equals(this.syntaxHighlightingPreset)) {
            this.syntaxHighlightingPreset = resolved.id();
            changed = true;
        }
    }

    public int getSnbtLineSpacing() {
        return Math.max(0, snbtLineSpacing);
    }

    public void setSnbtLineSpacing(int spacing) {
        int sanitized = Math.max(0, spacing);
        if (this.snbtLineSpacing != sanitized) {
            this.snbtLineSpacing = sanitized;
            changed = true;
        }
    }

    public int getSnbtSelectionBackgroundColor() {
        return snbtSelectionBackgroundColor == 0 ? 0x66FFFFFF : snbtSelectionBackgroundColor;
    }

    public void setSnbtSelectionBackgroundColor(int color) {
        if (this.snbtSelectionBackgroundColor != color) {
            this.snbtSelectionBackgroundColor = color;
            changed = true;
        }
    }

    public String getSnbtSelectionBackgroundColorHex() {
        return "0x%08X".formatted(getSnbtSelectionBackgroundColor());
    }

    public void setSnbtSelectionBackgroundColorHex(String value) {
        if (value == null) {
            return;
        }
        String sanitized = value.trim();
        if (sanitized.isEmpty()) {
            return;
        }
        try {
            int parsed = parseColorString(sanitized);
            setSnbtSelectionBackgroundColor(parsed);
        } catch (NumberFormatException ex) {
            LOGGER.warn("Invalid SNBT selection highlight color '{}'", value);
        }
    }

    private static int parseColorString(String raw) {
        String normalized = raw;
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
            normalized = normalized.substring(2);
        }
        if (normalized.length() > 8) {
            normalized = normalized.substring(normalized.length() - 8);
        }
        long parsed = Long.parseUnsignedLong(normalized, 16);
        return (int) parsed;
    }

    public static void load() {
        Path source = null;
        if (Files.exists(CLIENT_CONFIG_FILE)) {
            source = CLIENT_CONFIG_FILE;
        } else if (Files.exists(OLD_CLIENT_CONFIG_FILE)) {
            source = OLD_CLIENT_CONFIG_FILE;
        }

        if (source != null) {
            try (Reader r = Files.newBufferedReader(source)) {
                INSTANCE = GSON.fromJson(r, ClientConfiguration.class);
                LOGGER.info("客户端配置已加载");
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.error("加载客户端配置时出错", e);
                INSTANCE = new ClientConfiguration();
            }

            if (source.equals(OLD_CLIENT_CONFIG_FILE)) {
                changed = true;
                save();
                try {
                    Files.deleteIfExists(OLD_CLIENT_CONFIG_FILE);
                } catch (IOException ignored) {
                }
            }
        } else {
            LOGGER.info("生成默认客户端配置");
            INSTANCE = new ClientConfiguration();
            changed = true;
            save();
        }

        INSTANCE.normalize();
        DebugLog.modeChanged(INSTANCE.getGuapiDebugMode());

        if (INSTANCE.lastSeenUpdateLogVersion == null) {
            INSTANCE.lastSeenUpdateLogVersion = "";
        }
    }

    public static void save() {
        if (changed) {
            try (Writer w = Files.newBufferedWriter(CLIENT_CONFIG_FILE)) {
                GSON.toJson(INSTANCE, w);
                changed = false;
                LOGGER.info("客户端配置已保存");
            } catch (IOException e) {
                LOGGER.error("保存客户端配置时出错", e);
            }
        }
    }

    private void normalize() {
        if (version != 0) {
            LOGGER.warn("Unsupported client configuration version {}, falling back to defaults when needed", version);
        }
        if (guapiDebugMode == null) {
            guapiDebugMode = DebugMode.NONE;
        }
        if (syntaxHighlightingPreset == null || syntaxHighlightingPreset.isBlank()) {
            syntaxHighlightingPreset = SyntaxHighlightingPreset.LEGACY.id();
        }
        if (snbtLineSpacing < 0) {
            snbtLineSpacing = 2;
        }
        if (snbtSelectionBackgroundColor == 0) {
            snbtSelectionBackgroundColor = 0x66FFFFFF;
        }
    }
}
