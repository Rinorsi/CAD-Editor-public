package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.SoundEventSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ItemNoteBlockCategoryModel extends ItemEditorCategoryModel {
    private String soundId = "";
    private SoundEventSelectionEntryModel soundEntry;

    public ItemNoteBlockCategoryModel(ItemEditorModel editor) {
        super(ModTexts.NOTE_BLOCK_SOUND_CATEGORY, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        ResourceLocation current = stack.get(DataComponents.NOTE_BLOCK_SOUND);
        if (current != null) {
            soundId = current.toString();
        }
        String namespace = soundId.contains(":") ? soundId.substring(0, soundId.indexOf(':')) : "minecraft";
        soundEntry = new SoundEventSelectionEntryModel(this, ModTexts.NOTE_BLOCK_SOUND, soundId,
                value -> soundId = value == null ? "" : value.trim(), "namespace:" + namespace);
        getEntries().add(soundEntry);
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        if (soundId.isBlank()) {
            soundEntry.setValid(true);
            stack.remove(DataComponents.NOTE_BLOCK_SOUND);
            return;
        }
        ResourceLocation location = ClientUtil.parseResourceLocation(soundId);
        if (location == null) {
            soundEntry.setValid(false);
            return;
        }
        HolderLookup.RegistryLookup<SoundEvent> lookup = ClientUtil.registryAccess()
                .lookup(Registries.SOUND_EVENT)
                .orElse(null);
        if (lookup == null) {
            soundEntry.setValid(false);
            return;
        }
        Optional<Holder.Reference<SoundEvent>> holder = lookup.get(ResourceKey.create(Registries.SOUND_EVENT, location));
        if (holder.isEmpty()) {
            soundEntry.setValid(false);
            return;
        }
        soundEntry.setValid(true);
        stack.set(DataComponents.NOTE_BLOCK_SOUND, location);
    }
}
