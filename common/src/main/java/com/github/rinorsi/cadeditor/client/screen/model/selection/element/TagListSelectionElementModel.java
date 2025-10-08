package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class TagListSelectionElementModel extends ListSelectionElementModel {
    private final Component displayName;
    private final String lowerDisplay;

    public TagListSelectionElementModel(ResourceLocation id) {
        super(id.toString(), id);
        this.displayName = Component.literal("#" + id);
        this.lowerDisplay = displayName.getString().toLowerCase(Locale.ROOT);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public boolean matches(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        String lower = s.toLowerCase(Locale.ROOT);
        return getId().toString().toLowerCase(Locale.ROOT).contains(lower) || lowerDisplay.contains(lower);
    }

    @Override
    public Type getType() {
        return Type.TAG;
    }
}
