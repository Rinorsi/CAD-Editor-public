package com.github.rinorsi.cadeditor.client.screen.model.category.entity.player;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionEffectEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides editing for ActiveEffects of a player.
 */
public class EntityPlayerStatusEffectsCategoryModel extends EntityCategoryModel {
    private static final String EFFECTS_TAG = "ActiveEffects";

    public EntityPlayerStatusEffectsCategoryModel(EntityEditorModel editor) {
        super(Component.translatable("cadeditor.gui.player_effects"), editor);
    }

    @Override
    protected void setupEntries() {
        ListTag effects = ensurePlayerTag().getList(EFFECTS_TAG, Tag.TAG_COMPOUND);
        for (Tag element : effects) {
            if (element instanceof CompoundTag compound) {
                getEntries().add(createEffectEntry(compound));
            }
        }
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createEffectEntry(new CompoundTag());
    }

    @Override
    protected boolean canAddEntryInList() {
        return true;
    }

    @Override
    public void addEntryInList() {
        openEffectSelection();
    }

    @Override
    public int getEntryHeight() {
        return 50;
    }

    @Override
    public void apply() {
        super.apply();
        ListTag list = new ListTag();
        for (EntryModel entry : getEntries()) {
            if (!(entry instanceof PotionEffectEntryModel effect)) {
                continue;
            }
            String id = effect.getValue();
            if (id == null || id.isBlank()) {
                continue;
            }
            list.add(effect.toCompoundTag());
        }
        CompoundTag data = ensurePlayerTag();
        if (list.isEmpty()) {
            data.remove(EFFECTS_TAG);
        } else {
            data.put(EFFECTS_TAG, list);
        }
        getContext().setTag(data);
    }

    private PotionEffectEntryModel createEffectEntry(CompoundTag tag) {
        String id = "";
        int amplifier = 0;
        int duration = 200;
        boolean ambient = false;
        boolean showParticles = true;
        boolean showIcon = true;

        if (tag != null) {
            if (tag.contains("id", Tag.TAG_STRING)) {
                id = tag.getString("id");
            } else if (tag.contains("Id", Tag.TAG_BYTE)) {
                id = Integer.toString(Byte.toUnsignedInt(tag.getByte("Id")));
            }
            amplifier = tag.contains("amplifier", Tag.TAG_INT) ? tag.getInt("amplifier") : tag.getInt("Amplifier");
            if (tag.contains("duration", Tag.TAG_INT)) {
                duration = Math.max(1, tag.getInt("duration"));
            } else if (tag.contains("Duration", Tag.TAG_INT)) {
                duration = Math.max(1, tag.getInt("Duration"));
            }
            ambient = tag.contains("ambient", Tag.TAG_BYTE) ? tag.getBoolean("ambient") : tag.getBoolean("Ambient");
            showParticles = !tag.contains("show_particles", Tag.TAG_BYTE) || tag.getBoolean("show_particles");
            if (tag.contains("ShowParticles", Tag.TAG_BYTE)) {
                showParticles = tag.getBoolean("ShowParticles");
            }
            showIcon = !tag.contains("show_icon", Tag.TAG_BYTE) || tag.getBoolean("show_icon");
            if (tag.contains("ShowIcon", Tag.TAG_BYTE)) {
                showIcon = tag.getBoolean("ShowIcon");
            }
        }

        return new PotionEffectEntryModel(this, id, Math.max(0, amplifier), Math.max(1, duration), ambient, showParticles, showIcon, updated -> {});
    }

    private void openEffectSelection() {
        Set<ResourceLocation> current = collectEffectIds();
        ModScreenHandler.openListSelectionScreen(ModTexts.EFFECTS.copy(),
                "", ClientCache.getEffectSelectionItems(),
                value -> {}, true,
                this::applySelectedEffects,
                current);
    }

    private void applySelectedEffects(List<ResourceLocation> selected) {
        Map<ResourceLocation, PotionEffectEntryModel> existing = new LinkedHashMap<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof PotionEffectEntryModel effect) {
                ResourceLocation id = ResourceLocation.tryParse(effect.getValue());
                if (id != null) {
                    existing.putIfAbsent(id, effect);
                }
            }
        }
        List<PotionEffectEntryModel> desired = new ArrayList<>();
        if (selected != null) {
            for (ResourceLocation id : selected) {
                PotionEffectEntryModel entry = existing.remove(id);
                if (entry == null) {
                    entry = createEffectEntry(new CompoundTag());
                    entry.setValue(id.toString());
                }
                desired.add(entry);
            }
        }
        replaceEffectEntries(desired);
    }

    private void replaceEffectEntries(List<PotionEffectEntryModel> entries) {
        int start = getEntryListStart();
        int endExclusive = getEntries().size() - (canAddEntryInList() ? 1 : 0);
        for (int i = endExclusive - 1; i >= start; i--) {
            getEntries().remove(i);
        }
        getEntries().addAll(start, entries);
        updateEntryListIndexes();
    }

    private Set<ResourceLocation> collectEffectIds() {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof PotionEffectEntryModel effect) {
                ResourceLocation id = ResourceLocation.tryParse(effect.getValue());
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    private CompoundTag ensurePlayerTag() {
        CompoundTag data = getData();
        if (data == null) {
            data = new CompoundTag();
            getContext().setTag(data);
        }
        return data;
    }
}
