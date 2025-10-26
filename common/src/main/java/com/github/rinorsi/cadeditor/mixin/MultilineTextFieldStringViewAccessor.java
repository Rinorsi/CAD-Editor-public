package com.github.rinorsi.cadeditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.components.MultilineTextField$StringView")
public interface MultilineTextFieldStringViewAccessor {
    @Accessor("beginIndex")
    int cadeditor$getBeginIndex();

    @Accessor("endIndex")
    int cadeditor$getEndIndex();
}
