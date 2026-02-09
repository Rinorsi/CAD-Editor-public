package com.github.rinorsi.cadeditor.client.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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

    public static MutableComponent decode(Tag encoded, HolderLookup.Provider provider) {
        if (encoded == null) {
            return null;
        }
        if (encoded instanceof StringTag stringTag) {
            MutableComponent legacy = decode(stringTag.value(), provider);
            if (legacy != null) {
                return legacy;
            }
        }
        try {
            var ops = RegistryOps.create(NbtOps.INSTANCE, provider);
            return ComponentSerialization.CODEC.parse(ops, encoded)
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

    public static Tag encodeToTag(Component component, HolderLookup.Provider provider) {
        if (component == null) {
            return null;
        }
        try {
            var ops = RegistryOps.create(NbtOps.INSTANCE, provider);
            Tag encoded = ComponentSerialization.CODEC.encodeStart(ops, component)
                    .result()
                    .orElse(null);
            if (encoded != null) {
                return encoded;
            }
        } catch (Exception ignored) {
        }
        String legacyJson = encode(component, provider);
        return legacyJson.isEmpty() ? null : StringTag.valueOf(legacyJson);
    }
}
