package com.github.rinorsi.cadeditor.client.screen.skin;

import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import com.github.rinorsi.cadeditor.client.screen.widget.SyntaxHighlightingTextArea;
import com.github.rinorsi.cadeditor.client.util.texteditor.SNBTSyntaxHighlighter;
import com.github.rinorsi.cadeditor.client.util.texteditor.SNBTSyntaxHighlighter.Token;
import com.github.rinorsi.cadeditor.client.util.texteditor.SyntaxHighlightingPalette;
import com.github.rinorsi.cadeditor.client.util.texteditor.SyntaxHighlightingPreset;
import com.github.rinorsi.cadeditor.mixin.MultiLineEditBoxMixin;
import com.github.rinorsi.cadeditor.mixin.MultilineTextFieldMixin;
import com.github.rinorsi.cadeditor.mixin.MultilineTextFieldStringViewAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SyntaxHighlightingTextAreaSkinDelegate extends com.github.franckyi.guapi.base.theme.vanilla.delegate.VanillaTextAreaSkinDelegate<SyntaxHighlightingTextArea> {
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int TEXT_COLOR = -2039584;
    private static final int PLACEHOLDER_TEXT_COLOR = 0xCCFFFFFF;
    private static final int DEFAULT_SELECTION_BACKGROUND_COLOR = 0x66FFFFFF;
    private static final int DEFAULT_LINE_SPACING = 2;
    private static final int ERROR_TEXT_COLOR = 0xFFFF5555;
    private static final int TOKEN_ADVANCE_PADDING = 2;

    private final SyntaxHighlightingTextArea node;
    private final Font font;
    private long focusedTimestamp = Util.getMillis();

    @SuppressWarnings("this-escape")
    public SyntaxHighlightingTextAreaSkinDelegate(SyntaxHighlightingTextArea node) {
        super(node);
        this.node = node;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        MultilineTextField textField = resolveTextField();
        Font currentFont = resolveFont();
        if (textField == null || currentFont == null) {
            return;
        }
        String fullText = textField.value();
        SNBTSyntaxHighlighter highlighter = node.getHighlighter();
        highlighter.setSource(fullText);
        List<Token> tokens = highlighter.getTokens();
        SyntaxHighlightingPalette palette = SyntaxHighlightingPreset.resolveCurrent().palette();

        if (fullText.isEmpty() && !isFocused()) {
            Component placeholder = node.getPlaceholder();
            if (placeholder == null) {
                placeholder = Component.empty();
            }
            graphics.drawWordWrap(currentFont, placeholder, getX() + innerPadding(), getY() + innerPadding(), getWidth() - totalInnerPadding(), PLACEHOLDER_TEXT_COLOR);
            return;
        }

        int cursorIndex = textField.cursor();
        boolean shouldBlink = isFocused() && (Util.getMillis() - focusedTimestamp) / 300L % 2L == 0L;
        boolean cursorInText = cursorIndex < fullText.length();

        int caretX = getX() + innerPadding();
        int caretY = getY() + innerPadding();
        int lineY = getY() + innerPadding();
        int baseX = getX() + innerPadding();
        int lineHeightWithSpacing = getLineHeightWithSpacing();
        int parseErrorIndex = node.getParseErrorIndex();
        boolean hasParseError = parseErrorIndex >= 0;

        Iterable<?> visualLines = textField.iterateLines();
        for (Object view : visualLines) {
            int lineStart = beginIndex(view);
            int lineEnd = Math.min(fullText.length(), endIndex(view));
            boolean visible = withinContentAreaTopBottom(lineY, lineY + currentFont.lineHeight);

            if (shouldBlink && cursorInText && cursorIndex >= lineStart && cursorIndex <= lineEnd) {
                if (visible) {
                    caretX = drawRange(graphics, fullText, tokens, palette, lineStart, cursorIndex, baseX, lineY, hasParseError, parseErrorIndex);
                    graphics.fill(caretX, lineY - 1, caretX + CURSOR_INSERT_WIDTH, lineY + 1 + currentFont.lineHeight, CURSOR_INSERT_COLOR);
                    caretX = drawRange(graphics, fullText, tokens, palette, cursorIndex, lineEnd, caretX, lineY, hasParseError, parseErrorIndex);
                    caretY = lineY;
                }
            } else {
                if (visible) {
                    caretX = drawRange(graphics, fullText, tokens, palette, lineStart, lineEnd, baseX, lineY, hasParseError, parseErrorIndex);
                }
                caretY = lineY;
            }
            lineY += lineHeightWithSpacing;
        }

        if (shouldBlink && !cursorInText && withinContentAreaTopBottom(caretY, caretY + currentFont.lineHeight)) {
            graphics.drawString(currentFont, CURSOR_APPEND_CHARACTER, caretX, caretY, CURSOR_INSERT_COLOR);
        }

        if (textField.hasSelection()) {
            Object selected = textField.getSelected();
            int selectionBaseX = getX() + innerPadding();
            int selectionY = getY() + innerPadding();
            int selectionAnchor = ((MultilineTextFieldMixin) (Object) textField).getSelectCursor();
            int selectionStart = Math.min(cursorIndex, selectionAnchor);
            int selectionEnd = Math.max(cursorIndex, selectionAnchor);

            for (Object line : textField.iterateLines()) {
                if (beginIndex(selected) > endIndex(line)) {
                    selectionY += lineHeightWithSpacing;
                    continue;
                }
                if (beginIndex(line) > endIndex(selected)) {
                    break;
                }
                if (withinContentAreaTopBottom(selectionY, selectionY + currentFont.lineHeight)) {
                    int lineStart = beginIndex(line);
                    int lineEnd = Math.min(fullText.length(), endIndex(line));
                    int from = Math.max(selectionStart, lineStart);
                    int to = Math.min(selectionEnd, lineEnd);
                    String selectedText = from < to ? fullText.substring(from, to) : "";
                    int startX = selectionBaseX + currentFont.width(fullText.substring(lineStart, from));
                    int selectionTextEndX = startX + currentFont.width(selectedText);
                    int endX;
                    if (endIndex(selected) > lineEnd) {
                        endX = getX() + getWidth() - innerPadding();
                    } else {
                        endX = selectionTextEndX;
                    }
                    graphics.fill(startX, selectionY - 1, endX, selectionY + 1 + currentFont.lineHeight, getSelectionBackgroundColor());
                }
                selectionY += lineHeightWithSpacing;
            }
        }
    }

    private static int beginIndex(Object view) {
        return ((MultilineTextFieldStringViewAccessor) view).cadeditor$beginIndex();
    }

    private static int endIndex(Object view) {
        return ((MultilineTextFieldStringViewAccessor) view).cadeditor$endIndex();
    }

    private int drawSegment(GuiGraphics graphics, String fullText, List<Token> tokens, SyntaxHighlightingPalette palette, int start, int end, int x, int y) {
        int cursor = x;
        int index = start;

        for (Token token : tokens) {
            if (token.end() <= start) {
                continue;
            }
            if (token.start() >= end) {
                break;
            }
            if (index < Math.min(token.start(), end)) {
                int plainEnd = Math.min(token.start(), end);
                cursor = drawPlain(graphics, fullText.substring(index, plainEnd), cursor, y);
                index = plainEnd;
            }
            int colouredStart = Math.max(token.start(), start);
            int colouredEnd = Math.min(token.end(), end);
            if (colouredEnd > colouredStart) {
                cursor = drawColored(graphics, fullText.substring(colouredStart, colouredEnd), cursor, y, palette.colour(token.type()));
                index = colouredEnd;
            }
        }
        if (index < end) {
            cursor = drawPlain(graphics, fullText.substring(index, end), cursor, y);
        }
        return cursor;
    }

    private int drawRange(GuiGraphics graphics, String fullText, List<Token> tokens, SyntaxHighlightingPalette palette, int start, int end, int x, int y, boolean hasParseError, int parseErrorIndex) {
        if (start >= end) {
            return x;
        }
        int cursor = x;
        int normalEnd = hasParseError ? Math.min(end, parseErrorIndex) : end;
        if (normalEnd > start) {
            cursor = drawSegment(graphics, fullText, tokens, palette, start, normalEnd, cursor, y);
        }
        if (hasParseError && end > parseErrorIndex) {
            int errorStart = Math.max(parseErrorIndex, start);
            if (errorStart < end) {
                cursor = drawError(graphics, fullText.substring(errorStart, end), cursor, y);
            }
        }
        return cursor;
    }

    private int getLineHeightWithSpacing() {
        Font currentFont = resolveFont();
        int lineHeight = currentFont != null ? currentFont.lineHeight : 9;
        return lineHeight + getConfiguredLineSpacing();
    }

    private int drawPlain(GuiGraphics graphics, String text, int x, int y) {
        if (text.isEmpty()) {
            return x;
        }
        Font currentFont = resolveFont();
        int renderedX = currentFont == null ? x : graphics.drawString(currentFont, text, x, y, TEXT_COLOR);
        return clampCursorPosition(x, renderedX);
    }

    private int drawError(GuiGraphics graphics, String text, int x, int y) {
        if (text.isEmpty()) {
            return x;
        }
        Font currentFont = resolveFont();
        int renderedX = currentFont == null ? x : graphics.drawString(currentFont, text, x, y, ERROR_TEXT_COLOR);
        return clampCursorPosition(x, renderedX);
    }

    private int getSelectionBackgroundColor() {
        ClientConfiguration config = ClientConfiguration.INSTANCE;
        return config == null ? DEFAULT_SELECTION_BACKGROUND_COLOR : config.getSnbtSelectionBackgroundColor();
    }

    private int getConfiguredLineSpacing() {
        ClientConfiguration config = ClientConfiguration.INSTANCE;
        return config == null ? DEFAULT_LINE_SPACING : config.getSnbtLineSpacing();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isWithinContentArea(mouseX, mouseY) && button == 0) {
            MultilineTextField field = ((MultiLineEditBoxMixin) (Object) this).getTextField();
            field.setSelecting(Screen.hasShiftDown());
            ((MultiLineEditBoxMixin) (Object) this).invokeSeekCursorScreen(mouseX, adjustMouseY(mouseY));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (isWithinContentArea(mouseX, mouseY) && button == 0) {
            MultilineTextField field = ((MultiLineEditBoxMixin) (Object) this).getTextField();
            field.setSelecting(true);
            ((MultiLineEditBoxMixin) (Object) this).invokeSeekCursorScreen(mouseX, adjustMouseY(mouseY));
            field.setSelecting(Screen.hasShiftDown());
            return true;
        }
        return false;
    }

    private int drawColored(GuiGraphics graphics, String text, int x, int y, ChatFormatting colour) {
        if (text.isEmpty()) {
            return x;
        }
        Integer rgb = colour.getColor();
        int colourValue = rgb != null ? rgb : TEXT_COLOR;
        Font currentFont = resolveFont();
        int renderedX = currentFont == null ? x : graphics.drawString(currentFont, text, x, y, colourValue);
        return clampCursorPosition(x, renderedX);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            focusedTimestamp = Util.getMillis();
        }
    }

    @Override
    protected double scrollRate() {
        return getLineHeightWithSpacing() / 2.0;
    }

    @Override
    public int getInnerHeight() {
        MultilineTextField textField = resolveTextField();
        Font currentFont = resolveFont();
        if (textField == null || currentFont == null) {
            return super.getInnerHeight();
        }
        int lineCount = textField.getLineCount();
        if (lineCount <= 0) {
            return 0;
        }
        int spacing = Math.max(0, getConfiguredLineSpacing());
        return lineCount * currentFont.lineHeight + Math.max(0, (lineCount - 1) * spacing);
    }

    private int clampCursorPosition(int start, int renderedEnd) {
        int adjusted = renderedEnd - 3 + TOKEN_ADVANCE_PADDING;
        int maxX = getX() + getWidth() - innerPadding();
        if (adjusted > maxX) {
            adjusted = maxX;
        }
        if (adjusted < start) {
            adjusted = start;
        }
        return adjusted;
    }

    private double adjustMouseY(double mouseY) {
        int spacing = getConfiguredLineSpacing();
        if (spacing <= 0) {
            return mouseY;
        }
        Font currentFont = resolveFont();
        if (currentFont == null) {
            return mouseY;
        }
        double innerTop = getY() + innerPadding();
        double contentY = mouseY - innerTop + scrollAmount();
        if (contentY <= 0) {
            return mouseY;
        }
        double lineSpan = currentFont.lineHeight + spacing;
        int lines = (int) Math.floor(contentY / lineSpan);
        double adjustment = lines * spacing;
        if (adjustment <= 0) {
            return mouseY;
        }
        return mouseY - adjustment;
    }

    private boolean isWithinContentArea(double mouseX, double mouseY) {
        int innerLeft = getX() + innerPadding();
        int innerTop = getY() + innerPadding();
        int innerRight = innerLeft + (getWidth() - totalInnerPadding());
        int innerBottom = innerTop + getInnerHeight();
        return mouseX >= innerLeft && mouseX < innerRight && mouseY >= innerTop && mouseY < innerBottom;
    }

    private MultilineTextField resolveTextField() {
        return ((MultiLineEditBoxMixin) (Object) this).getTextField();
    }

    private Font resolveFont() {
        Font currentFont = this.font;
        if (currentFont != null) {
            return currentFont;
        }
        return Minecraft.getInstance().font;
    }
}
