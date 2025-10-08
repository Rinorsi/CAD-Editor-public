package com.github.franckyi.guapi.api.util;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.chat.Component;

public enum DebugMode {
    @SerializedName(value = "INFO")
    INFO("cadeditor.gui.debug_mode.info"),
    @SerializedName(value = "FEATURE", alternate = {"FULL"})
    FEATURE("cadeditor.gui.debug_mode.feature"),
    @SerializedName(value = "UI", alternate = {"HOVER"})
    UI("cadeditor.gui.debug_mode.ui"),
    @SerializedName(value = "NONE", alternate = {"OFF"})
    NONE("cadeditor.gui.debug_mode.none");

    private final String translationKey;

    DebugMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Component toComponent() {
        return Component.translatable(translationKey);
    }
}
