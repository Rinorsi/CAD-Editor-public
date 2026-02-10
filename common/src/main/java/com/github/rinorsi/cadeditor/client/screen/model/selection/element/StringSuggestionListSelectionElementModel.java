package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import com.github.rinorsi.cadeditor.common.ModConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Locale;

public class StringSuggestionListSelectionElementModel extends ListSelectionElementModel {
    private static final String PREFIX = "string/";
    private final String value;

    public StringSuggestionListSelectionElementModel(String value) {
        super(value, toResourceLocation(value));
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public MutableComponent getDisplayName() {
        return Component.literal(value);
    }

    @Override
    public boolean matches(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        String lower = s.toLowerCase(Locale.ROOT);
        return value.toLowerCase(Locale.ROOT).contains(lower)
                || getId().toString().toLowerCase(Locale.ROOT).contains(lower);
    }

    private static Identifier toResourceLocation(String value) {
        Identifier parsed = Identifier.tryParse(value);
        if (parsed != null) {
            return parsed;
        }
        String hex = HexFormat.of().formatHex(value.getBytes(StandardCharsets.UTF_8));
        return Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, PREFIX + hex);
    }
}
