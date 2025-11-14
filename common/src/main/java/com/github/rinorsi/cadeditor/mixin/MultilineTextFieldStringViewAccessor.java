package com.github.rinorsi.cadeditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "net.minecraft.client.gui.components.MultilineTextField$StringView")
public interface MultilineTextFieldStringViewAccessor {
    @Accessor(value = "comp_862", remap = false)
    int cadeditor$beginIndex();

    @Accessor(value = "comp_863", remap = false)
    int cadeditor$endIndex();
}
