package com.github.rinorsi.cadeditor.mixin;

import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultilineTextField.StringView.class)
public interface MultilineTextFieldStringViewAccessor {
    @Accessor("beginIndex")
    int cadeditor$getBeginIndex();

    @Accessor("endIndex")
    int cadeditor$getEndIndex();
}
