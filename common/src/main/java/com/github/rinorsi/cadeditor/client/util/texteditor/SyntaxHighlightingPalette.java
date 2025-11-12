package com.github.rinorsi.cadeditor.client.util.texteditor;

import net.minecraft.ChatFormatting;

public final class SyntaxHighlightingPalette {
    private final ChatFormatting key;
    private final ChatFormatting string;
    private final ChatFormatting number;
    private final ChatFormatting bool;
    private final ChatFormatting nullColor;
    private final ChatFormatting symbol;
    private final ChatFormatting separator;

    public SyntaxHighlightingPalette(ChatFormatting key,
                                     ChatFormatting string,
                                     ChatFormatting number,
                                     ChatFormatting bool,
                                     ChatFormatting nullColor,
                                     ChatFormatting symbol,
                                     ChatFormatting separator) {
        this.key = key;
        this.string = string;
        this.number = number;
        this.bool = bool;
        this.nullColor = nullColor;
        this.symbol = symbol;
        this.separator = separator;
    }

    public ChatFormatting colour(SNBTSyntaxHighlighter.TokenType type) {
        return switch (type) {
            case KEY -> key;
            case STRING -> string;
            case NUMBER -> number;
            case BOOLEAN -> bool;
            case NULL -> nullColor;
            case SYMBOL -> symbol;
            case SEPARATOR -> separator;
        };
    }
}
