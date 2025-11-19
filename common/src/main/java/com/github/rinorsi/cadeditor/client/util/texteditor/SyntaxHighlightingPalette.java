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
    private final ChatFormatting component;
    private final ChatFormatting componentError;
    private final ChatFormatting syntaxError;

    public SyntaxHighlightingPalette(ChatFormatting key,
                                     ChatFormatting string,
                                     ChatFormatting number,
                                     ChatFormatting bool,
                                     ChatFormatting nullColor,
                                     ChatFormatting symbol,
                                     ChatFormatting separator,
                                     ChatFormatting component,
                                     ChatFormatting componentError,
                                     ChatFormatting syntaxError) {
        this.key = key;
        this.string = string;
        this.number = number;
        this.bool = bool;
        this.nullColor = nullColor;
        this.symbol = symbol;
        this.separator = separator;
        this.component = component;
        this.componentError = componentError;
        this.syntaxError = syntaxError;
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
            case COMPONENT_KEY -> component;
            case COMPONENT_ERROR -> componentError;
            case ERROR -> syntaxError;
        };
    }
}
