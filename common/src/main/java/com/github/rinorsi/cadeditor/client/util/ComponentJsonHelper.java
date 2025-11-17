package com.github.rinorsi.cadeditor.client.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

/**
 * Thin wrapper around {@link ComponentSerialization} that keeps the JSON based workflow we used
 * before Mojang moved everything to codecs.
 */
public final class ComponentJsonHelper {
    private ComponentJsonHelper() {
    }

    public static MutableComponent decode(String json, HolderLookup.Provider provider) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            var ops = RegistryOps.create(JsonOps.INSTANCE, provider);
            return ComponentSerialization.CODEC.parse(ops, element)
                    .result()
                    .map(Component::copy)
                    .map(MutableComponent.class::cast)
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String encode(Component component, HolderLookup.Provider provider) {
        if (component == null) {
            return "";
        }
        var ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        return ComponentSerialization.CODEC.encodeStart(ops, component)
                .result()
                .map(JsonElement::toString)
                .orElse("");
    }
}
