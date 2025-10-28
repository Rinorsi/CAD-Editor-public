package com.github.rinorsi.cadeditor.client.util.texteditor;

import com.github.franckyi.guapi.api.node.TextField;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Parses SNBT text into coloured tokens so Minecraft's native {@link TextField} can render syntax highlighting.
 * Tokenization happens when {@link #setSource(String)} receives changes; {@link #render(String, int)} simply
 * selects the visible slice without reparsing every frame.
 */
public final class SNBTSyntaxHighlighter implements TextField.TextRenderer {

    public static final class Token {
        private final int start;
        private final int end;
        private final TokenType type;

        Token(int start, int end, TokenType type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }

        public int start() {
            return start;
        }

        public int end() {
            return end;
        }

        public TokenType type() {
            return type;
        }
    }

    public enum TokenType {
        KEY,
        STRING,
        NUMBER,
        BOOLEAN,
        NULL,
        SYMBOL,
        SEPARATOR
    }

    private String source = "";
    private List<Token> tokens = List.of();

    public void setSource(String text) {
        String updated = text == null ? "" : text;
        if (Objects.equals(source, updated)) {
            return;
        }
        source = updated;
        tokens = List.copyOf(tokenize(source));
    }

    @Override
    public Component render(String view, int firstCharacterIndex) {
        if (view == null || view.isEmpty()) {
            return Component.empty();
        }
        int absoluteStart = Math.max(0, firstCharacterIndex);
        int absoluteEnd = Math.min(source.length(), absoluteStart + view.length());
        if (absoluteStart >= absoluteEnd) {
            return Component.literal(view);
        }
        MutableComponent result = Component.empty();
        int cursor = absoluteStart;
        SyntaxHighlightingPalette palette = SyntaxHighlightingPreset.resolveCurrent().palette();
        for (Token token : tokens) {
            if (token.end <= absoluteStart) {
                continue;
            }
            if (token.start >= absoluteEnd) {
                break;
            }
            if (cursor < Math.min(token.start, absoluteEnd)) {
                int plainEnd = Math.min(token.start, absoluteEnd);
                if (plainEnd > cursor) {
                    result.append(Component.literal(source.substring(cursor, plainEnd)));
                    cursor = plainEnd;
                }
            }
            int colouredStart = Math.max(token.start, absoluteStart);
            int colouredEnd = Math.min(token.end, absoluteEnd);
            if (colouredEnd > colouredStart) {
                result.append(Component.literal(source.substring(colouredStart, colouredEnd))
                        .withStyle(palette.colour(token.type)));
                cursor = colouredEnd;
            }
        }
        if (cursor < absoluteEnd) {
            result.append(Component.literal(source.substring(cursor, absoluteEnd)));
        }
        return result;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private static List<Token> tokenize(String text) {
        int length = text.length();
        if (length == 0) {
            return List.of();
        }
        List<Token> result = new ArrayList<>();
        int i = 0;
        while (i < length) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '"' || c == '\'') {
                int start = i;
                i++;
                boolean escape = false;
                while (i < length) {
                    char ch = text.charAt(i);
                    if (escape) {
                        escape = false;
                    } else if (ch == '\\') {
                        escape = true;
                    } else if (ch == c) {
                        i++;
                        break;
                    }
                    i++;
                }
                TokenType type = lookaheadIsKey(text, i) ? TokenType.KEY : TokenType.STRING;
                result.add(new Token(start, Math.min(i, length), type));
                continue;
            }
            if (isNumberStart(c, text, i)) {
                int start = i;
                if (c == '-') {
                    i++;
                }
                while (i < length && Character.isDigit(text.charAt(i))) {
                    i++;
                }
                if (i < length && text.charAt(i) == '.') {
                    i++;
                    while (i < length && Character.isDigit(text.charAt(i))) {
                        i++;
                    }
                }
                if (i < length && isNumericSuffix(text.charAt(i))) {
                    i++;
                }
                result.add(new Token(start, i, TokenType.NUMBER));
                continue;
            }
            if (isIdentifierStart(c)) {
                int start = i;
                i++;
                while (i < length && isIdentifierPart(text.charAt(i))) {
                    i++;
                }
                String word = text.substring(start, i);
                String lower = word.toLowerCase(Locale.ROOT);
                if ("true".equals(lower) || "false".equals(lower)) {
                    result.add(new Token(start, i, TokenType.BOOLEAN));
                } else if ("null".equals(lower)) {
                    result.add(new Token(start, i, TokenType.NULL));
                } else if (lookaheadIsKey(text, i)) {
                    result.add(new Token(start, i, TokenType.KEY));
                }
                continue;
            }
            if ("{}[]()".indexOf(c) >= 0) {
                result.add(new Token(i, i + 1, TokenType.SYMBOL));
                i++;
                continue;
            }
            if (":,=".indexOf(c) >= 0) {
                result.add(new Token(i, i + 1, TokenType.SEPARATOR));
                i++;
                continue;
            }
            i++;
        }
        return result;
    }

    private static boolean lookaheadIsKey(String text, int index) {
        int i = index;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i < text.length() && (text.charAt(i) == ':' || text.charAt(i) == '=');
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '$';
    }

    private static boolean isNumberStart(char c, String text, int index) {
        return Character.isDigit(c) || (c == '-' && index + 1 < text.length() && Character.isDigit(text.charAt(index + 1)));
    }

    private static boolean isNumericSuffix(char c) {
        return "bBsSlLfFdD".indexOf(c) >= 0;
    }
}
