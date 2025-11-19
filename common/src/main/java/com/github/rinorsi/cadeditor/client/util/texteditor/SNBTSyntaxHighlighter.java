package com.github.rinorsi.cadeditor.client.util.texteditor;

import com.github.franckyi.guapi.api.node.TextField;
import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.util.SnbtHelper;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        SEPARATOR,
        COMPONENT_KEY,
        COMPONENT_ERROR,
        ERROR
    }

    private String source = "";
    private List<Token> tokens = List.of();
    private List<ComponentKeyInfo> componentKeys = List.of();
    private List<ComponentRegion> componentRegions = List.of();
    private int syntaxErrorOffset = -1;

    public void setSource(String text) {
        String updated = text == null ? "" : text;
        if (Objects.equals(source, updated)) {
            return;
        }
        source = updated;
        ComponentAnalysis analysis = analyzeComponents(source, tokenize(source));
        syntaxErrorOffset = detectSyntaxErrorOffset(source);
        tokens = List.copyOf(applySyntaxErrorTokens(analysis.tokens(), source.length(), syntaxErrorOffset));
        componentKeys = analysis.componentKeys();
        componentRegions = analysis.componentRegions();
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

    public List<ComponentKeyInfo> getComponentKeys() {
        return componentKeys;
    }

    public int getSyntaxErrorOffset() {
        return syntaxErrorOffset;
    }

    public List<ComponentRegion> getComponentRegions() {
        return componentRegions;
    }

    public boolean isInsideComponentRegion(int index) {
        if (componentRegions.isEmpty()) {
            return false;
        }
        for (ComponentRegion region : componentRegions) {
            if (region.contains(index)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getInvalidComponentIds() {
        if (componentKeys.isEmpty()) {
            return List.of();
        }
        List<String> invalid = new ArrayList<>();
        for (ComponentKeyInfo key : componentKeys) {
            if (!key.valid()) {
                invalid.add(key.rawId());
            }
        }
        return invalid;
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

    private static ComponentAnalysis analyzeComponents(String text, List<Token> originalTokens) {
        if (originalTokens.isEmpty()) {
            return new ComponentAnalysis(List.of(), List.of(), List.of());
        }
        List<ComponentKeyInfo> keys = new ArrayList<>();
        List<ComponentRegion> regions = new ArrayList<>();
        Deque<ComponentScope> scope = new ArrayDeque<>();
        boolean awaitingComponentsCompound = false;
        Map<Integer, ComponentKeyInfo> tokenInfo = new HashMap<>();

        for (Token token : originalTokens) {
            TokenType type = token.type();
            if (type == TokenType.KEY) {
                String keyText = readKeyText(text, token);
                if ("components".equals(keyText)) {
                    awaitingComponentsCompound = true;
                } else if (!scope.isEmpty() && scope.peek().isComponents()) {
                    ComponentKeyInfo info = buildComponentInfo(keyText, token);
                    keys.add(info);
                    tokenInfo.put(token.start(), info);
                } else {
                    awaitingComponentsCompound = false;
                }
            } else if (type == TokenType.SYMBOL) {
                char symbol = text.charAt(token.start());
                if (symbol == '{') {
                    boolean isComponentsScope = awaitingComponentsCompound;
                    scope.push(new ComponentScope(isComponentsScope, token.start()));
                    awaitingComponentsCompound = false;
                } else if (symbol == '}') {
                    ComponentScope popped = scope.isEmpty() ? null : scope.pop();
                    if (popped != null && popped.isComponents()) {
                        regions.add(new ComponentRegion(popped.start(), token.end()));
                    }
                    awaitingComponentsCompound = false;
                } else {
                    awaitingComponentsCompound = false;
                }
            } else if (type != TokenType.SEPARATOR) {
                awaitingComponentsCompound = false;
            }
        }

        List<Token> annotated = new ArrayList<>(originalTokens.size());
        for (Token token : originalTokens) {
            TokenType type = token.type();
            ComponentKeyInfo info = tokenInfo.get(token.start());
            if (info != null) {
                type = info.valid() ? TokenType.COMPONENT_KEY : TokenType.COMPONENT_ERROR;
            }
            if (type == token.type()) {
                annotated.add(token);
            } else {
                annotated.add(new Token(token.start(), token.end(), type));
            }
        }
        return new ComponentAnalysis(List.copyOf(annotated), List.copyOf(keys), List.copyOf(regions));
    }

    private static ComponentKeyInfo buildComponentInfo(String rawKey, Token token) {
        boolean removal = rawKey != null && !rawKey.isEmpty() && rawKey.charAt(0) == '!';
        String normalized = removal ? rawKey.substring(1) : rawKey;
        boolean valid = ClientCache.isComponentIdKnown(normalized);
        return new ComponentKeyInfo(token.start(), token.end(), rawKey, normalized, valid, removal);
    }

    private static String readKeyText(String text, Token token) {
        String raw = text.substring(token.start(), token.end());
        if (raw.length() >= 2) {
            char first = raw.charAt(0);
            char last = raw.charAt(raw.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return raw.substring(1, raw.length() - 1);
            }
        }
        return raw;
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

    public record ComponentKeyInfo(int start, int end, String rawId, String normalizedId, boolean valid, boolean removal) {
    }

    public record ComponentRegion(int start, int end) {
        public boolean contains(int index) {
            return index >= start && index <= end;
        }
    }

    private record ComponentScope(boolean isComponents, int start) {
    }

    private record ComponentAnalysis(List<Token> tokens, List<ComponentKeyInfo> componentKeys, List<ComponentRegion> componentRegions) {
    }

    private static int detectSyntaxErrorOffset(String text) {
        if (text == null || text.isEmpty()) {
            return -1;
        }
        try {
            SnbtHelper.parse(text);
            return -1;
        } catch (CommandSyntaxException e) {
            return Math.max(0, e.getCursor());
        }
    }

    private static List<Token> applySyntaxErrorTokens(List<Token> baseTokens, int length, int errorOffset) {
        if (errorOffset < 0 || errorOffset > length) {
            return baseTokens;
        }
        List<Token> result = new ArrayList<>();
        for (Token token : baseTokens) {
            if (token.end() <= errorOffset) {
                result.add(token);
                continue;
            }
            if (token.start() < errorOffset) {
                result.add(new Token(token.start(), errorOffset, token.type()));
            }
            break;
        }
        result.add(new Token(Math.min(errorOffset, length), length, TokenType.ERROR));
        return result;
    }
}
