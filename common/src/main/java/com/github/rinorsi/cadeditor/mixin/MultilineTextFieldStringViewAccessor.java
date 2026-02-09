package com.github.rinorsi.cadeditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accesses methods on MultilineTextField.StringView via target name because it
 * is not publicly accessible in all mappings.
 */
@SuppressWarnings("public-target")
@Mixin(targets = "net.minecraft.client.gui.components.MultilineTextField$StringView", remap = true)
public interface MultilineTextFieldStringViewAccessor {
    @Invoker("beginIndex")
    int cadeditor$beginIndex();

    @Invoker("endIndex")
    int cadeditor$endIndex();
}
