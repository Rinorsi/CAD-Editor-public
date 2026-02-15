package com.github.rinorsi.cadeditor.client.screen.skin;

import com.github.rinorsi.cadeditor.client.screen.widget.SyntaxHighlightingTextArea;
import com.github.rinorsi.cadeditor.client.util.texteditor.SNBTSyntaxHighlighter;
import com.github.rinorsi.cadeditor.client.util.texteditor.SNBTSyntaxHighlighter.Token;
import com.github.rinorsi.cadeditor.client.util.texteditor.SyntaxHighlightingPalette;
import com.github.rinorsi.cadeditor.client.util.texteditor.SyntaxHighlightingPreset;
import com.github.rinorsi.cadeditor.mixin.MultiLineEditBoxMixin;
import com.github.rinorsi.cadeditor.mixin.MultilineTextFieldMixin;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.List;

public class SyntaxHighlightingTextAreaSkinDelegate extends com.github.franckyi.guapi.base.theme.vanilla.delegate.VanillaTextAreaSkinDelegate<SyntaxHighlightingTextArea> {
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int TEXT_COLOR = -2039584;
    private static final int PLACEHOLDER_TEXT_COLOR = 0xCCFFFFFF;
    private static final int SELECTION_COLOR = 0x66FFFFFF;
    private static final int ERROR_TEXT_COLOR = 0xFFFF6666;
    private static final int TOKEN_ADVANCE_PADDING = 2;
    private static final int LINE_SPACING = 2;

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
        int errorCursor = node.getErrorCursor();

        int caretX = getX() + innerPadding();
        int caretY = getY() + innerPadding();
        int lineY = getY() + innerPadding();
        int baseX = getX() + innerPadding();
        int lineHeightWithSpacing = font.lineHeight + LINE_SPACING;

        Iterable<?> visualLines = textField.iterateLines();
        for (Object view : visualLines) {
            int lineStart = beginIndex(view);
            int lineEnd = Math.min(fullText.length(), endIndex(view));
            boolean visible = withinContentAreaTopBottom(lineY, lineY + font.lineHeight);

            if (shouldBlink && cursorInText && cursorIndex >= lineStart && cursorIndex <= lineEnd) {
                if (visible) {
                    caretX = drawSegment(graphics, fullText, highlighter, palette, lineStart, cursorIndex, baseX, lineY);
                    graphics.fill(caretX, lineY - 1, caretX + CURSOR_INSERT_WIDTH, lineY + 1 + font.lineHeight, CURSOR_INSERT_COLOR);
                    caretX = drawSegment(graphics, fullText, highlighter, palette, cursorIndex, lineEnd, caretX, lineY);
                    caretY = lineY;
                }
            } else {
                if (visible) {
                    caretX = drawSegment(graphics, fullText, highlighter, palette, lineStart, lineEnd, baseX, lineY);
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
                    int endX = endIndex(selected) > lineEnd
                            ? getX() + getWidth() - innerPadding()
                            : selectionBaseX + font.width(fullText.substring(lineStart, to));
                    graphics.fill(startX, selectionY - 1, endX, selectionY + 1 + font.lineHeight, SELECTION_COLOR);
                }
                selectionY += lineHeightWithSpacing;
            }
        }

        renderErrorOverlay(graphics, fullText, errorCursor, baseX, lineHeightWithSpacing);
    }

    private void renderErrorOverlay(GuiGraphics graphics, String fullText, int errorCursor, int baseX, int lineHeightWithSpacing) {
        if (errorCursor < 0 || errorCursor >= fullText.length()) {
            return;
        }
        int lineY = getY() + innerPadding();
        boolean highlightRest = false;
        for (Object view : textField.iterateLines()) {
            int lineStart = beginIndex(view);
            int lineEnd = Math.min(fullText.length(), endIndex(view));
            int drawStart = -1;
            if (!highlightRest) {
                if (errorCursor > lineEnd) {
                    // no highlight on this line
                } else if (errorCursor >= lineStart) {
                    drawStart = errorCursor;
                    highlightRest = true;
                }
            } else {
                drawStart = lineStart;
            }
            if (drawStart >= 0 && withinContentAreaTopBottom(lineY, lineY + font.lineHeight)) {
                int startX = baseX + font.width(fullText.substring(lineStart, drawStart));
                String segment = fullText.substring(drawStart, lineEnd);
                graphics.drawString(font, segment, startX, lineY, ERROR_TEXT_COLOR);
            }
            lineY += lineHeightWithSpacing;
        }
    }

    private int drawSegment(GuiGraphics graphics, String fullText, SNBTSyntaxHighlighter highlighter, SyntaxHighlightingPalette palette,
                             int start, int end, int x, int y) {
        int cursor = x;
        int index = start;
        List<Token> tokens = highlighter.getTokens();

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

    private int drawPlain(GuiGraphics graphics, String text, int x, int y) {
        if (text.isEmpty()) {
            return x;
        }
        int renderedX = graphics.drawString(font, text, x, y, TEXT_COLOR);
        return clampCursorPosition(x, renderedX);
    }

    private int drawColored(GuiGraphics graphics, String text, int x, int y, ChatFormatting colour) {
        if (text.isEmpty()) {
            return x;
        }
        Integer rgb = colour.getColor();
        int colourValue = rgb != null ? rgb : TEXT_COLOR;
        int renderedX = graphics.drawString(font, text, x, y, colourValue);
        return clampCursorPosition(x, renderedX);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            focusedTimestamp = Util.getMillis();
        }
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

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.withinContentAreaPoint(d, e) && i == 0) {
            ((MultiLineEditBoxMixin) (Object) this).getTextField().setSelecting(net.minecraft.client.gui.screens.Screen.hasShiftDown());
            double adjustedY = adjustMouseY(e);
            ((MultiLineEditBoxMixin) (Object) this).invokeSeekCursorScreen(d, adjustedY);
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseDragged(double d, double e, int button, double dx, double dy) {
        return super.mouseDragged(d, adjustMouseY(e), button, dx, dy);
    }

    private double adjustMouseY(double screenY) {
        double contentTop = getY() + innerPadding();
        double local = screenY - contentTop;
        if (local <= 0) {
            return screenY;
        }
        double perLine = font.lineHeight + LINE_SPACING;
        if (perLine <= 0) {
            return screenY;
        }
        int lineIndex = (int)Math.floor(local / perLine);
        if (lineIndex < 0) lineIndex = 0;
        double offsetWithinLine = local - lineIndex * perLine;
        if (offsetWithinLine < 0) offsetWithinLine = 0;
        if (offsetWithinLine > font.lineHeight) offsetWithinLine = font.lineHeight;
        double adjustedLocal = lineIndex * font.lineHeight + offsetWithinLine;
        return contentTop + adjustedLocal;
    }

    private static int beginIndex(Object view) {
        return getComponentIndex(view, 0);
    }

    private static int endIndex(Object view) {
        return getComponentIndex(view, 1);
    }

    private static int getComponentIndex(Object view, int componentIndex) {
        Class<?> cls = view.getClass();
        try {
            RecordComponent[] components = cls.getRecordComponents();
            if (components != null && components.length > componentIndex) {
                Method accessor = components[componentIndex].getAccessor();
                accessor.setAccessible(true);
                Object value = accessor.invoke(view);
                if (value instanceof Integer integer) {
                    return integer;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        int intFieldIndex = 0;
        for (Field field : cls.getDeclaredFields()) {
            if (field.getType() != int.class) {
                continue;
            }
            if (intFieldIndex == componentIndex) {
                try {
                    field.setAccessible(true);
                    return field.getInt(view);
                } catch (IllegalAccessException ignored) {
                }
                break;
            }
            intFieldIndex++;
        }
        throw new IllegalStateException("Unable to read StringView component " + componentIndex + " from " + cls.getName());
    }
}
