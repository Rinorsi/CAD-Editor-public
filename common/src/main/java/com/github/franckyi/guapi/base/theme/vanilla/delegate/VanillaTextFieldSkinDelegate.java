package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.TextField;
import com.github.rinorsi.cadeditor.mixin.EditBoxMixin;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("this-escape")
public class VanillaTextFieldSkinDelegate<N extends TextField> extends EditBox implements VanillaWidgetSkinDelegate {
    private final N node;
    private final EditBoxMixin self;

    public VanillaTextFieldSkinDelegate(N node) {
        super(Minecraft.getInstance().font, node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.getLabel());
        this.node = node;
        self = (EditBoxMixin) this;
        active = !node.isDisabled();
        setMaxLength(node.getMaxLength());
        setValue(node.getText());
        setFocused(node.isFocused());
        setResponder(node::setText);
        node.xProperty().addListener(newVal -> setX(newVal + 1));
        node.yProperty().addListener(newVal -> setY(newVal + 1));
        node.widthProperty().addListener(newVal -> setWidth(newVal - 2));
        node.heightProperty().addListener(newVal -> height = newVal - 2);
        node.disabledProperty().addListener(newVal -> active = !newVal);
        node.labelProperty().addListener(this::setMessage);
        node.maxLengthProperty().addListener(this::setMaxLength);
        node.textProperty().addListener(this::updateText);
        node.focusedProperty().addListener(this::setFocused);
        node.validatorProperty().addListener(this::updateValidator);
        node.validationForcedProperty().addListener(this::updateValidator);
        node.textRendererProperty().addListener(this::updateRenderer);
        node.cursorPositionProperty().addListener(super::setCursorPosition);
        node.highlightPositionProperty().addListener(super::setHighlightPos);
        node.placeholderProperty().addListener(this::updatePlaceholder);
        node.textProperty().addListener(this::updatePlaceholder);
        moveCursorToStart(false); // fix in order to render text
        updateValidator();
        updateRenderer();
        updatePlaceholder();
    }

    private void updateText(String text) {
        if (node.getValidator().test(text)) {
            if (text.length() > node.getMaxLength()) {
                self.setRawValue(text.substring(0, node.getMaxLength()));
            } else {
                self.setRawValue(text);
            }
        }
    }

    private void updateValidator() {
        if (node.isValidationForced()) {
            if (node.getValidator() == null) {
                setFilter(Objects::nonNull);
            } else {
                setFilter(node.getValidator());
            }
        } else {
            setFilter(Objects::nonNull);
        }
    }

    private void updateRenderer() {
        if (node.getTextRenderer() == null) {
            setFormatter((string, integer) -> FormattedCharSequence.forward(string, Style.EMPTY));
        } else {
            setFormatter((string, integer) -> renderText(string, integer).getVisualOrderText());
        }
        moveCursorToStart(false); // fix in order to render text
    }

    private void updatePlaceholder() {
        setSuggestion(getValue().isEmpty() ? node.getPlaceholder().getString() : null);
    }

    public Component renderText(String str, int firstCharacterIndex) {
        return node.getTextRenderer() == null ? Component.literal(str) : node.getTextRenderer().render(str, firstCharacterIndex);
    }

    @Override
    public void setCursorPosition(int value) {
        super.setCursorPosition(value);
        node.setCursorPosition(getCursorPosition());
        if (getCursorPosition() < self.getDisplayPos()) {
            self.setDisplayPos(getCursorPosition());
        }
    }

    @Override
    public void setHighlightPos(int value) {
        super.setHighlightPos(value);
        node.setHighlightPosition(self.getHighlightPos());
    }

    @Override
    public void insertText(@NotNull String string) {
        int oldCursorPos = getCursorPosition();
        int oldHighlightPos = node.getHighlightPosition();
        String oldText = getValue();
        super.insertText(string);
        node.onTextUpdate(oldCursorPos, oldHighlightPos, oldText, getCursorPosition(), getValue());
    }

    @Override
    public void deleteChars(int characterOffset) {
        if (getHighlighted().isEmpty()) {
            int oldCursorPos = getCursorPosition();
            int oldHighlightPos = node.getHighlightPosition();
            String oldText = getValue();
            super.deleteChars(characterOffset);
            node.onTextUpdate(oldCursorPos, oldHighlightPos, oldText, getCursorPosition(), getValue());
        } else {
            super.deleteChars(characterOffset);
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        int displayPos = self.getDisplayPos();
        Font font = Minecraft.getInstance().font;
        FormattedText string = font.substrByWidth(renderText(getValue().substring(displayPos), displayPos), getInnerWidth());
        setHighlightPos(font.substrByWidth(string, Mth.floor(mouseX) - getX() - 4).getString().length() + displayPos);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Font font = Minecraft.getInstance().font;
        if (!isVisible()) {
            return false;
        } else {
            boolean flag = mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height;
            if (self.canLoseFocus() && button == 0) {
                setFocused(flag);
            }

            if (isFocused() && flag && button == 0) {
                int i = Mth.floor(mouseX) - getX();
                if (self.isBordered()) {
                    i -= 4;
                }

                FormattedText string = font.substrByWidth(renderText(getValue().substring(self.getDisplayPos()), self.getDisplayPos()), getInnerWidth());
                moveCursorTo(font.substrByWidth(string, i).getString().length() + self.getDisplayPos(), false);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        Font font = Minecraft.getInstance().font;
        if (isVisible()) {
            if (self.isBordered()) {
                int i = isFocused() ? -1 : -6250336;
                guiGraphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, i);
                guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, -16777216);
            }

            int i2 = self.isEditable() ? self.getTextColor() : self.getTextColorUneditable();
            int j = self.getCursorPos() - self.getDisplayPos();
            int k = self.getHighlightPos() - self.getDisplayPos();
            Component renderedText = renderText(getValue().substring(self.getDisplayPos()), self.getDisplayPos());
            String s = renderedText != null
                    ? font.substrByWidth(renderedText, getInnerWidth()).getString()
                    : font.plainSubstrByWidth(getValue().substring(self.getDisplayPos()), this.getInnerWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = isFocused() && (Util.getMillis() - self.getFocusedTime()) / 300L % 2 == 0 && flag;
            int l = self.isBordered() ? getX() + 4 : getX();
            int i1 = self.isBordered() ? getY() + (height - 8) / 2 : getY();
            int j1 = l;
            if (k > s.length()) {
                k = s.length();
            }

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = guiGraphics.drawString(font, self.getFormatter().apply(s1, self.getDisplayPos()), l, i1, i2);
            }

            boolean flag2 = self.getCursorPos() < getValue().length() || getValue().length() >= self.invokeGetMaxLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                guiGraphics.drawString(font, self.getFormatter().apply(s.substring(j), self.getCursorPos()), j1, i1, i2);
            }

            if (!flag2 && self.getSuggestion() != null) {
                guiGraphics.drawString(font, self.getSuggestion(), (k1 - 1), i1, -8355712);
            }

            if (flag1) {
                if (flag2) {
                    guiGraphics.fill(k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
                } else {
                    guiGraphics.drawString(font, "_", k1, i1, i2);
                }
            }

            if (k != j) {
                int firstCharacterIndex = self.getDisplayPos();
                int cursorPosition = self.getCursorPos();
                int highlightPosition = self.getHighlightPos();
                int start = Math.max(Math.min(cursorPosition, highlightPosition), firstCharacterIndex);
                int end = Math.max(cursorPosition, highlightPosition);
                Component fullText = renderText(getValue().substring(firstCharacterIndex), firstCharacterIndex);
                String trimmedText = font.substrByWidth(fullText, getInnerWidth()).getString();
                if (cursorPosition == end && end > trimmedText.length() + firstCharacterIndex) {
                    self.setDisplayPos(end - trimmedText.length());
                }
                Component previousText = renderText(getValue().substring(firstCharacterIndex, start), firstCharacterIndex);
                int previousTextWidth = font.width(previousText);
                Component highlightedText = renderText(getValue().substring(start, end), start);
                int highlightedTextWidth = font.width(highlightedText);
                int x0 = getX() + 4;
                self.invokeRenderHighlight(guiGraphics, x0 + previousTextWidth, i1 - 1, x0 + previousTextWidth + highlightedTextWidth, i1 + 1 + 9);
            }

        }
    }
}
