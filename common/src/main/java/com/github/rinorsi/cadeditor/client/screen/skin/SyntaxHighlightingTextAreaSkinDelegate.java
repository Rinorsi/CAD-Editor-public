package com.github.rinorsi.cadeditor.client.screen.skin;

import com.github.franckyi.guapi.api.RenderHelper;
import com.github.rinorsi.cadeditor.client.screen.widget.SyntaxHighlightingTextArea;
import com.github.rinorsi.cadeditor.client.util.texteditor.SNBTSyntaxHighlighter;
import com.github.rinorsi.cadeditor.client.util.texteditor.SNBTSyntaxHighlighter.Token;
import com.github.rinorsi.cadeditor.client.util.texteditor.SyntaxHighlightingPalette;
import com.github.rinorsi.cadeditor.client.util.texteditor.SyntaxHighlightingPreset;
import com.github.rinorsi.cadeditor.mixin.MultiLineEditBoxMixin;
import com.github.rinorsi.cadeditor.mixin.MultilineTextFieldMixin;
import com.github.rinorsi.cadeditor.mixin.MultilineTextFieldStringViewAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SyntaxHighlightingTextAreaSkinDelegate extends com.github.franckyi.guapi.base.theme.vanilla.delegate.VanillaTextAreaSkinDelegate<SyntaxHighlightingTextArea> {
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int TEXT_COLOR = -2039584;
    private static final int PLACEHOLDER_TEXT_COLOR = 0xCCFFFFFF;
    private static final int SELECTION_BACKGROUND_COLOR = 0x66FFFFFF;
    private static final int LINE_SPACING = 2;
    private static final int SYNTAX_ERROR_COLOR = 0xFFFF6A6A;

    private final SyntaxHighlightingTextArea node;
    private final MultilineTextField textField;
    private final Font font;
    private long focusedTimestamp = Util.getMillis();

    @SuppressWarnings("this-escape")
    public SyntaxHighlightingTextAreaSkinDelegate(SyntaxHighlightingTextArea node) {
        super(node);
        this.node = node;
        this.textField = ((MultiLineEditBoxMixin) (Object) this).getTextField();
        this.font = Minecraft.getInstance().font;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        String fullText = textField.value();
        SNBTSyntaxHighlighter highlighter = node.getHighlighter();
        highlighter.setSource(fullText);
        SyntaxHighlightingPalette palette = SyntaxHighlightingPreset.resolveCurrent().palette();

        if (fullText.isEmpty() && !isFocused()) {
            Component placeholder = node.getPlaceholder();
            if (placeholder == null) {
                placeholder = Component.empty();
            }
            graphics.drawWordWrap(font, placeholder, getX() + innerPadding(), getY() + innerPadding(), getWidth() - totalInnerPadding(), PLACEHOLDER_TEXT_COLOR);
            return;
        }

        int cursorIndex = textField.cursor();
        boolean shouldBlink = isFocused() && (Util.getMillis() - focusedTimestamp) / 300L % 2L == 0L;
        boolean cursorInText = cursorIndex < fullText.length();

        int caretX = getX() + innerPadding();
        int caretY = getY() + innerPadding();
        int lineY = getY() + innerPadding();
        int baseX = getX() + innerPadding();
        int lineHeightWithSpacing = font.lineHeight + LINE_SPACING;
        int errorOffset = highlighter.getSyntaxErrorOffset();

        Iterable<?> visualLines = textField.iterateLines();
        for (Object view : visualLines) {
            int lineStart = beginIndex(view);
            int lineEnd = Math.min(fullText.length(), endIndex(view));
            boolean visible = withinContentAreaTopBottom(lineY, lineY + font.lineHeight);

            if (shouldBlink && cursorInText && cursorIndex >= lineStart && cursorIndex <= lineEnd) {
                if (visible) {
                    caretX = drawSegment(graphics, fullText, palette, lineStart, cursorIndex, baseX, lineY, errorOffset);
                    graphics.fill(caretX, lineY - 1, caretX + CURSOR_INSERT_WIDTH, lineY + 1 + font.lineHeight, CURSOR_INSERT_COLOR);
                    caretX = drawSegment(graphics, fullText, palette, cursorIndex, lineEnd, caretX, lineY, errorOffset);
                    caretY = lineY;
                }
            } else {
                if (visible) {
                    caretX = drawSegment(graphics, fullText, palette, lineStart, lineEnd, baseX, lineY, errorOffset);
                }
                caretY = lineY;
            }
            lineY += lineHeightWithSpacing;
        }

        if (shouldBlink && !cursorInText && withinContentAreaTopBottom(caretY, caretY + font.lineHeight)) {
            graphics.drawString(font, CURSOR_APPEND_CHARACTER, caretX, caretY, CURSOR_INSERT_COLOR);
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
                if (withinContentAreaTopBottom(selectionY, selectionY + font.lineHeight)) {
                    int lineStart = beginIndex(line);
                    int lineEnd = Math.min(fullText.length(), endIndex(line));
                    int from = Math.max(selectionStart, lineStart);
                    int to = Math.min(selectionEnd, lineEnd);
                    int startX = selectionBaseX + font.width(fullText.substring(lineStart, from));
                    int endX;
                    if (endIndex(selected) > lineEnd) {
                        endX = getX() + getWidth() - innerPadding();
                    } else {
                        endX = selectionBaseX + font.width(fullText.substring(lineStart, to));
                    }
                    graphics.fill(startX, selectionY - 1, endX, selectionY + 1 + font.lineHeight, SELECTION_BACKGROUND_COLOR);
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

    private int drawSegment(GuiGraphics graphics, String fullText, SyntaxHighlightingPalette palette, int start, int end, int x, int y, int errorOffset) {
        int cursor = x;
        int index = start;
        List<Token> tokens = node.getHighlighter().getTokens();

        for (Token token : tokens) {
            if (token.end() <= start) {
                continue;
            }
            if (token.start() >= end) {
                break;
            }
            if (index < Math.min(token.start(), end)) {
                int plainEnd = Math.min(token.start(), end);
                cursor = drawPlain(graphics, fullText, index, plainEnd, cursor, y, errorOffset);
                index = plainEnd;
            }
            int colouredStart = Math.max(token.start(), start);
            int colouredEnd = Math.min(token.end(), end);
            if (colouredEnd > colouredStart) {
                cursor = drawColored(graphics, fullText, colouredStart, colouredEnd, cursor, y, palette.colour(token.type()), errorOffset);
                index = colouredEnd;
            }
        }
        if (index < end) {
            cursor = drawPlain(graphics, fullText, index, end, cursor, y, errorOffset);
        }
        return cursor;
    }

    private int drawPlain(GuiGraphics graphics, String fullText, int start, int end, int x, int y, int errorOffset) {
        return drawWithColour(graphics, fullText, start, end, x, y, RenderHelper.ensureOpaqueColor(TEXT_COLOR), errorOffset);
    }

    private int drawColored(GuiGraphics graphics, String fullText, int start, int end, int x, int y, ChatFormatting colour, int errorOffset) {
        Integer rgb = colour.getColor();
        int colourValue = RenderHelper.ensureOpaqueColor(rgb != null ? rgb : TEXT_COLOR);
        return drawWithColour(graphics, fullText, start, end, x, y, colourValue, errorOffset);
    }

    private int drawWithColour(GuiGraphics graphics, String fullText, int start, int end, int x, int y, int colour, int errorOffset) {
        if (start >= end) {
            return x;
        }
        if (errorOffset >= 0 && end > errorOffset) {
            if (start < errorOffset) {
                x = drawWithColour(graphics, fullText, start, errorOffset, x, y, colour, -1);
                start = errorOffset;
            }
            return drawWithColour(graphics, fullText, start, end, x, y, RenderHelper.ensureOpaqueColor(SYNTAX_ERROR_COLOR), -1);
        }
        String slice = fullText.substring(start, end);
        graphics.drawString(font, slice, x, y, colour);
        return x + font.width(slice);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            focusedTimestamp = Util.getMillis();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (isPointInsideContentArea(event.x(), event.y()) && event.button() == 0) {
            textField.setSelecting((event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0);
            seekCursorWithSpacing(event.x(), event.y());
            return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (super.mouseDragged(event, deltaX, deltaY)) {
            return true;
        }
        if (isPointInsideContentArea(event.x(), event.y()) && event.button() == 0) {
            textField.setSelecting(true);
            seekCursorWithSpacing(event.x(), event.y());
            textField.setSelecting((event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0);
            return true;
        }
        return false;
    }

    private void seekCursorWithSpacing(double mouseX, double mouseY) {
        double localX = mouseX - (double) getX() - (double) innerPadding();
        double visualY = mouseY - (double) getY() - (double) innerPadding() + scrollAmount();
        double logicalY = convertVisualYToLogical(visualY);
        textField.seekCursorToPoint(localX, logicalY);
    }

    private double convertVisualYToLogical(double visualY) {
        if (LINE_SPACING <= 0) {
            return visualY;
        }
        double perLine = font.lineHeight + LINE_SPACING;
        if (perLine <= 0) {
            return visualY;
        }
        double clamped = Math.max(0.0, visualY);
        int lineIndex = (int) (clamped / perLine);
        double offset = clamped - lineIndex * perLine;
        offset = Math.min(offset, font.lineHeight);
        return lineIndex * font.lineHeight + offset;
    }

    private boolean isPointInsideContentArea(double mouseX, double mouseY) {
        int innerLeft = getX() + innerPadding();
        int innerTop = getY() + innerPadding();
        int innerRight = innerLeft + (getWidth() - totalInnerPadding());
        int innerBottom = innerTop + Math.max(getInnerHeight(), 0);
        return mouseX >= innerLeft && mouseX < innerRight && mouseY >= innerTop && mouseY < innerBottom;
    }

    @Override
    public int getInnerHeight() {
        if (textField == null) {
            return super.getInnerHeight();
        }
        int lines = Math.max(1, textField.getLineCount());
        int spacing = Math.max(0, lines - 1) * LINE_SPACING;
        return lines * font.lineHeight + spacing;
    }

    @Override
    public double scrollRate() {
        return (double) (font.lineHeight + LINE_SPACING) / 2.0;
    }
}
