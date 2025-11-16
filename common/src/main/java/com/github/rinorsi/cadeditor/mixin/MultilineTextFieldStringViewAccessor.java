package com.github.rinorsi.cadeditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "net.minecraft.client.gui.components.MultilineTextField$StringView")
public interface MultilineTextFieldStringViewAccessor {
    @Invoker("beginIndex")
    int cadeditor$beginIndex();

    @Invoker("endIndex")
    int cadeditor$endIndex();
}
