    package com.github.rinorsi.cadeditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.components.MultilineTextField$StringView", remap = true)
public interface MultilineTextFieldStringViewAccessor {
    @Invoker("beginIndex")
    int cadeditor$beginIndex();

    @Invoker("endIndex")
    int cadeditor$endIndex();
}
