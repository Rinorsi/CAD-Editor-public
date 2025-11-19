package com.github.rinorsi.cadeditor.client.util.texteditor;

import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum SyntaxHighlightingPreset {
    LEGACY("legacy", new SyntaxHighlightingPalette(
            ChatFormatting.YELLOW,
            ChatFormatting.GREEN,
            ChatFormatting.GOLD,
            ChatFormatting.AQUA,
            ChatFormatting.DARK_GRAY,
            ChatFormatting.GRAY,
            ChatFormatting.DARK_GRAY,
            ChatFormatting.AQUA,
            ChatFormatting.RED,
            ChatFormatting.DARK_RED
    )),
    VSCODE("vscode", new SyntaxHighlightingPalette(
            ChatFormatting.GOLD,
            ChatFormatting.RED,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.BLUE,
            ChatFormatting.DARK_GRAY,
            ChatFormatting.GRAY,
            ChatFormatting.DARK_AQUA,
            ChatFormatting.AQUA,
            ChatFormatting.RED,
            ChatFormatting.DARK_RED
    )),
    MONOKAI("monokai", new SyntaxHighlightingPalette(
            ChatFormatting.GREEN,
            ChatFormatting.GOLD,
            ChatFormatting.AQUA,
            ChatFormatting.BLUE,
            ChatFormatting.DARK_GRAY,
            ChatFormatting.GRAY,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.AQUA,
            ChatFormatting.RED,
            ChatFormatting.DARK_RED
    )),
    SOLARIZED("solarized", new SyntaxHighlightingPalette(
            ChatFormatting.DARK_AQUA,
            ChatFormatting.GOLD,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.BLUE,
            ChatFormatting.DARK_GRAY,
            ChatFormatting.GRAY,
            ChatFormatting.DARK_AQUA,
            ChatFormatting.AQUA,
            ChatFormatting.RED,
            ChatFormatting.DARK_RED
    ));

    private final String id;
    private final SyntaxHighlightingPalette palette;

    SyntaxHighlightingPreset(String id, SyntaxHighlightingPalette palette) {
        this.id = id;
        this.palette = palette;
    }

    public String id() {
        return id;
    }

    public SyntaxHighlightingPalette palette() {
        return palette;
    }

    public MutableComponent toComponent() {
        return Component.translatable("cadeditor.gui.syntax_highlighting_preset." + id);
    }

    public static SyntaxHighlightingPreset byId(String id) {
        if (id == null || id.isBlank()) {
            return LEGACY;
        }
        for (SyntaxHighlightingPreset preset : values()) {
            if (preset.id.equalsIgnoreCase(id)) {
                return preset;
            }
        }
        return LEGACY;
    }

    public static SyntaxHighlightingPreset resolveCurrent() {
        ClientConfiguration configuration = ClientConfiguration.INSTANCE;
        if (configuration == null) {
            return LEGACY;
        }
        return configuration.getSyntaxHighlightingPreset();
    }
}
