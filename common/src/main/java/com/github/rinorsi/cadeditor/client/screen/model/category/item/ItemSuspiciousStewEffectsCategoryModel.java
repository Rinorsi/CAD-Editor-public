package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionEffectEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.SuspiciousStewEffects.Entry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Editor for Suspicious Stew effects (minecraft:suspicious_stew_effects).
 * Uses standard potion effect rows but only persists id+duration.
 */
@SuppressWarnings("unused")
public class ItemSuspiciousStewEffectsCategoryModel extends ItemEditorCategoryModel {
    private final List<Entry> stagedEffects = new ArrayList<>();

    public ItemSuspiciousStewEffectsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("suspicious_stew_effects"), editor);
    }

    @Override
    protected void setupEntries() {
        List<EffectData> effects = readEffects();
        if (effects.isEmpty()) {
            getEntries().add(createEffectEntry(null));
        } else {
            for (EffectData e : effects) {
                getEntries().add(createEffectEntry(e));
            }
        }
    }

    private List<EffectData> readEffects() {
        ItemStack stack = getParent().getContext().getItemStack();
        SuspiciousStewEffects component = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
        if (component != null) {
            List<EffectData> effects = new ArrayList<>();
            component.effects().forEach(entry -> {
                String id = entry.effect()
                        .unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse("minecraft:empty");
                effects.add(new EffectData(id, entry.duration()));
            });
            if (!effects.isEmpty()) {
                return effects;
            }
        }

        CompoundTag data = getData();
        if (data == null) return List.of();
        CompoundTag comps = data.getCompound("components").orElse(null);
        if (comps == null) return List.of();
        ListTag list = comps.getList("minecraft:suspicious_stew_effects").orElse(null);
        if (list == null) return List.of();
        List<EffectData> out = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i).orElse(null);
            if (c == null) continue;
            String id = NbtHelper.getString(c, "id", "");
            if (id.isEmpty()) continue;
            int duration = c.getIntOr("duration", 160);
            out.add(new EffectData(id, duration));
        }
        return out;
    }

    @Override
    public int getEntryListStart() { return 0; }

    @Override
    public EntryModel createNewListEntry() { return createEffectEntry(null); }

    @Override
    public void addEntryInList() {
        openEffectSelection();
    }

    @Override
    public int getEntryHeight() { return 50; }

    @Override
    public void apply() {
        stagedEffects.clear();
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        if (stagedEffects.isEmpty()) {
            stack.remove(DataComponents.SUSPICIOUS_STEW_EFFECTS);
        } else {
            stack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(List.copyOf(stagedEffects)));
        }
    }

    private EntryModel createEffectEntry(EffectData data) {
        if (data != null) {
            return new PotionEffectEntryModel(this, data.id(), 0, data.duration(), false, true, true, this::collectEffect);
        }
        String defaultId = MobEffects.SPEED.unwrapKey()
                .map(key -> key.location().toString())
                .orElse("minecraft:movement_speed");
        return new PotionEffectEntryModel(this, defaultId, 0, 160, false, true, true, this::collectEffect);
    }

    private void collectEffect(PotionEffectEntryModel entry) {
        ResourceLocation rl = ResourceLocation.tryParse(entry.getValue());
        if (rl == null) return;
        var lookupOpt = ClientUtil.registryAccess().lookup(Registries.MOB_EFFECT);
        if (lookupOpt.isEmpty()) return;
        var holder = lookupOpt.get().get(ResourceKey.create(Registries.MOB_EFFECT, rl));
        if (holder.isEmpty()) return;

        int duration = Math.max(1, entry.getDuration());
        stagedEffects.add(new Entry(holder.get(), duration));
    }

    private record EffectData(String id, int duration) {}

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
                    entry = (PotionEffectEntryModel) createEffectEntry(new EffectData(id.toString(), 160));
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
}
