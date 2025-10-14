package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.Locale;

public class SoundEventListSelectionElementModel extends ListSelectionElementModel {
    private final Component displayName;
    private final String namespace;
    private final String searchName;
    private final String subtitleSearch;

    public SoundEventListSelectionElementModel(ResourceLocation id, SoundEvent event) {
        super(id.toString(), id);
        this.namespace = id.getNamespace();
        Component translated = Component.translatable(Util.makeDescriptionId("sound_event", id));
        this.displayName = translated;
        this.searchName = translated.getString().toLowerCase(Locale.ROOT);
        this.subtitleSearch = "";
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean matches(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        String lower = s.toLowerCase(Locale.ROOT);
        return super.matches(s) || searchName.contains(lower) || (!subtitleSearch.isEmpty() && subtitleSearch.contains(lower));
    }
}
