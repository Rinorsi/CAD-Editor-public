package com.github.rinorsi.cadeditor.mixin;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditBox.class)
public interface EditBoxMixin {
    @Accessor("value")
    void setRawValue(String text);

    @Accessor("focusedTime")
    long getFocusedTime();

    @Accessor("bordered")
    boolean isBordered();

    @Accessor("canLoseFocus")
    boolean canLoseFocus();

    @Accessor("isEditable")
    boolean isEditable();

    @Accessor("displayPos")
    int getDisplayPos();

    @Accessor("displayPos")
    void setDisplayPos(int start);

    @Accessor("cursorPos")
    int getCursorPos();

    @Accessor("highlightPos")
    int getHighlightPos();

    @Accessor("textColor")
    int getTextColor();

    @Accessor("textColorUneditable")
    int getTextColorUneditable();

    @Accessor("suggestion")
    String getSuggestion();

    @Invoker("getMaxLength")
    int invokeGetMaxLength();
}
