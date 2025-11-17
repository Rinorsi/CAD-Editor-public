package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
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

public class EntityStatusEffectsCategoryModel extends EntityCategoryModel {
    private static final String EFFECTS_TAG = "ActiveEffects";

    public EntityStatusEffectsCategoryModel(EntityEditorModel editor) {
        super(Component.translatable("cadeditor.gui.player_effects"), editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = getData();
        if (data == null) {
            return;
        }
        ListTag effects = data.getList(EFFECTS_TAG).orElseGet(ListTag::new);
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
            CompoundTag tag = effect.toCompoundTag();
            list.add(tag);
        }
        CompoundTag data = ensureTag();
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
            id = tag.getString("id")
                    .or(() -> tag.getString("Id"))
                    .orElse(id);
            if (id.isEmpty()) {
                id = tag.getInt("Id").map(value -> Integer.toString(value)).orElse(id);
            }
            amplifier = tag.getIntOr("amplifier", tag.getIntOr("Amplifier", amplifier));
            duration = Math.max(1, tag.getIntOr("duration", tag.getIntOr("Duration", duration)));
            duration = Math.max(1, duration);
            ambient = tag.getBoolean("ambient").orElse(tag.getBooleanOr("Ambient", ambient));
            showParticles = tag.getBoolean("show_particles")
                    .or(() -> tag.getBoolean("ShowParticles"))
                    .orElse(true);
            showIcon = tag.getBoolean("show_icon")
                    .or(() -> tag.getBoolean("ShowIcon"))
                    .orElse(true);
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

    private CompoundTag ensureTag() {
        CompoundTag data = getData();
        if (data == null) {
            data = new CompoundTag();
            getContext().setTag(data);
        }
        return data;
    }
}
