package com.github.rinorsi.cadeditor.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(CompoundTag.class)
public interface CompoundTagMixin {
    @Invoker("entrySet")
    Set<Map.Entry<String, Tag>> getEntries();
}
