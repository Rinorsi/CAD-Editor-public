package com.github.rinorsi.cadeditor.client.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

/**
 * Helper around the 1.21.8 SNBT parser API so existing call sites can keep the simple
 * {@code parse(String)} semantics we used prior to the ValueInput rewrite.
 */
public final class SnbtHelper {
    private SnbtHelper() {}

    public static CompoundTag parse(String input) throws CommandSyntaxException {
        return TagParser.parseCompoundFully(input);
    }
}
