package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionEffectEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public class ItemPotionEffectsCategoryModel extends ItemEditorCategoryModel {
    private List<CompoundTag> collectedCustomEffects;
    private List<CompoundTag> collectedBaseEffects;
    private List<CompoundTag> originalBaseEffectTags = List.of();
    private int originalBaseEntryCount;
    private int currentBaseEntryCount;
    private boolean baseEffectsModified;

    public ItemPotionEffectsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.POTION_EFFECTS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        String potionId = "";
        int customColor = Color.NONE;
        originalBaseEffectTags = List.of();
        originalBaseEntryCount = 0;
        if (contents != null) {
            potionId = contents.potion()
                    .flatMap(h -> h.unwrapKey().map(k -> java.util.Optional.of(k.location().toString())).orElse(java.util.Optional.empty()))
                    .orElse("");
            customColor = contents.customColor().orElse(Color.NONE);
            originalBaseEffectTags = resolveBasePotionEffects(potionId);
            originalBaseEntryCount = originalBaseEffectTags.size();
            originalBaseEffectTags.forEach(tag -> getEntries().add(createPotionEffectEntry(tag, true)));
            contents.customEffects().forEach(e -> getEntries().add(createPotionEffectEntry(toTag(e), false)));
        } else {
            potionId = getTag().getString("Potion");
            customColor = getCustomPotionColor();
            originalBaseEffectTags = resolveBasePotionEffects(potionId);
            originalBaseEntryCount = originalBaseEffectTags.size();
            originalBaseEffectTags.forEach(tag -> getEntries().add(createPotionEffectEntry(tag, true)));
            getTag().getList("custom_potion_effects", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(t -> createPotionEffectEntry(t, false))
                    .forEach(getEntries()::add);
        }
        getEntries().add(0, new PotionSelectionEntryModel(this, ModTexts.DEFAULT_POTION,
                potionId, customColor,
                p -> getOrCreateTag().putString("Potion", p), this::setCustomPotionColor));
    }

    @Override
    public int getEntryListStart() {
        return 1;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createPotionEffectEntry(null, false);
    }

    @Override
    public void addEntryInList() {
        openCustomEffectSelection();
    }

    @Override
    public int getEntryHeight() {
        return 50;
    }

    @Override
    protected MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.EFFECT;
    }

    @Override
    public void apply() {
        collectedCustomEffects = new ArrayList<>();
        collectedBaseEffects = new ArrayList<>();
        baseEffectsModified = false;
        currentBaseEntryCount = 0;
        super.apply();
        if (currentBaseEntryCount != originalBaseEntryCount) {
            baseEffectsModified = true;
        } else if (!baseEffectsModified && !tagsEqual(collectedBaseEffects, originalBaseEffectTags)) {
            baseEffectsModified = true;
        }
        ItemStack stack = getParent().getContext().getItemStack();
        var registry = ClientUtil.registryAccess();
        var potionLookupOpt = registry.lookup(Registries.POTION);
        var effectLookupOpt = registry.lookup(Registries.MOB_EFFECT);
        List<MobEffectInstance> effects = new ArrayList<>();
        List<CompoundTag> effectiveEffects = new ArrayList<>(collectedCustomEffects);
        if (baseEffectsModified) {
            effectiveEffects.addAll(collectedBaseEffects);
        }
        if (effectLookupOpt.isPresent()) {
            var effectLookup = effectLookupOpt.get();
            for (CompoundTag c : effectiveEffects) {
                String id = c.getString("id");
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl == null) {
                    continue;
                }
                var holderOpt = effectLookup.get(ResourceKey.create(Registries.MOB_EFFECT, rl));
                if (holderOpt.isEmpty()) {
                    continue;
                }
                int amplifier = c.getInt("amplifier");
                int duration = c.contains("duration", Tag.TAG_INT) ? c.getInt("duration") : 1;
                boolean ambient = c.getBoolean("ambient");
                boolean showParticles = !c.contains("show_particles", Tag.TAG_BYTE) || c.getBoolean("show_particles");
                boolean showIcon = c.contains("show_icon", Tag.TAG_BYTE) && c.getBoolean("show_icon");
                effects.add(new MobEffectInstance(holderOpt.get(), duration, amplifier, ambient, showParticles, showIcon));
            }
        }
        String potionStr = getOrCreateTag().getString("Potion");
        if (baseEffectsModified && !potionStr.isEmpty()) {
            potionStr = "";
            getOrCreateTag().putString("Potion", potionStr);
            if (!getEntries().isEmpty() && getEntries().get(0) instanceof PotionSelectionEntryModel selection) {
                selection.setValue(potionStr);
                selection.apply();
            }
        }
        int customColor = getCustomPotionColor();
        PotionContents contents = null;
        if (potionLookupOpt.isPresent()) {
            var potionLookup = potionLookupOpt.get();
            ResourceLocation rl = potionStr.isEmpty() ? ResourceLocation.parse("minecraft:empty") : ResourceLocation.tryParse(potionStr);
            java.util.Optional<Holder<Potion>> pot = java.util.Optional.empty();
            if (!baseEffectsModified && rl != null) {
                var potHolder = potionLookup.get(ResourceKey.create(Registries.POTION, rl));
                if (potHolder.isPresent()) {
                    pot = java.util.Optional.of(potHolder.get());
                }
            }
            contents = new PotionContents(pot,
                    customColor != Color.NONE ? java.util.Optional.of(customColor) : java.util.Optional.empty(),
                    effects);
        }
        if (contents != null) {
            stack.set(DataComponents.POTION_CONTENTS, contents);
        } else {
            stack.remove(DataComponents.POTION_CONTENTS);
        }
        if (getData().contains("tag", Tag.TAG_COMPOUND)) {
            var tag = getTag();
            tag.remove("Potion");
            tag.remove("CustomPotionColor");
            tag.remove("custom_potion_effects");
        }
    }

    private int getCustomPotionColor() {
        return getTag().contains("CustomPotionColor", Tag.TAG_INT) ? getTag().getInt("CustomPotionColor") : Color.NONE;
    }

    private void setCustomPotionColor(int color) {
        if (color != Color.NONE) {
            getOrCreateTag().putInt("CustomPotionColor", color);
        } else {
            getOrCreateTag().remove("CustomPotionColor");
        }
    }

    private EntryModel createPotionEffectEntry(CompoundTag tag, boolean baseEffect) {
        if (tag != null) {
            String id = tag.getString("id");
            int amplifier = tag.getInt("amplifier");
            int duration = tag.contains("duration", Tag.TAG_INT) ? tag.getInt("duration") : 1;
            boolean ambient = tag.getBoolean("ambient");
            boolean showParticles = !tag.contains("show_particles", Tag.TAG_BYTE) || tag.getBoolean("show_particles");
            boolean showIcon = tag.getBoolean("show_icon");
            return new PotionEffectEntryModel(this, id, amplifier, duration, ambient, showParticles, showIcon,
                    this::collectPotionEffect, baseEffect, tag);
        }
        String defaultId = MobEffects.MOVEMENT_SPEED.unwrapKey()
                .map(key -> key.location().toString())
                .orElse("minecraft:movement_speed");
        return new PotionEffectEntryModel(this, defaultId, 0, 1, false, true, true, this::collectPotionEffect);
    }

    private void collectPotionEffect(PotionEffectEntryModel entry) {
        CompoundTag tag = entry.toCompoundTag();
        if (entry.isBaseEffect()) {
            currentBaseEntryCount++;
            collectedBaseEffects.add(tag.copy());
            if (entry.isModifiedFromOriginal()) {
                baseEffectsModified = true;
            }
        } else {
            collectedCustomEffects.add(tag.copy());
        }
    }

    private List<CompoundTag> resolveBasePotionEffects(String potionId) {
        if (potionId == null || potionId.isEmpty()) {
            return List.of();
        }
        var lookupOpt = ClientUtil.registryAccess().lookup(Registries.POTION);
        if (lookupOpt.isEmpty()) {
            return List.of();
        }
        ResourceLocation rl = ResourceLocation.tryParse(potionId);
        if (rl == null) {
            return List.of();
        }
        var holderOpt = lookupOpt.get().get(ResourceKey.create(Registries.POTION, rl));
        if (holderOpt.isEmpty()) {
            return List.of();
        }
        List<CompoundTag> result = new ArrayList<>();
        holderOpt.get().value().getEffects().forEach(effect -> result.add(toTag(effect)));
        return List.copyOf(result);
    }

    private static boolean tagsEqual(List<CompoundTag> a, List<CompoundTag> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static CompoundTag toTag(MobEffectInstance e) {
        CompoundTag tag = new CompoundTag();
        String id = e.getEffect().unwrapKey().map(k -> k.location().toString()).orElse("");
        tag.putString("id", id);
        tag.putInt("amplifier", e.getAmplifier());
        tag.putInt("duration", e.getDuration());
        tag.putBoolean("ambient", e.isAmbient());
        tag.putBoolean("show_particles", e.isVisible());
        tag.putBoolean("show_icon", e.showIcon());
        return tag;
    }

    private void openCustomEffectSelection() {
        Set<ResourceLocation> current = collectCustomEffectIds();
        ModScreenHandler.openListSelectionScreen(ModTexts.EFFECTS.copy(),
                "", ClientCache.getEffectSelectionItems(),
                value -> {}, true,
                this::applyCustomSelection,
                current);
    }

    private void applyCustomSelection(List<ResourceLocation> selected) {
        Map<ResourceLocation, PotionEffectEntryModel> existing = new LinkedHashMap<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof PotionEffectEntryModel effect && !effect.isBaseEffect()) {
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
                    entry = (PotionEffectEntryModel) createPotionEffectEntry(null, false);
                    entry.setValue(id.toString());
                }
                desired.add(entry);
            }
        }
        replaceCustomEffectEntries(desired);
    }

    private void replaceCustomEffectEntries(List<PotionEffectEntryModel> customEntries) {
        int insertionIndex = getEntryListStart();
        while (insertionIndex < getEntries().size()) {
            EntryModel model = getEntries().get(insertionIndex);
            if (model instanceof PotionEffectEntryModel effect && effect.isBaseEffect()) {
                insertionIndex++;
                continue;
            }
            break;
        }
        int endExclusive = getEntries().size() - (canAddEntryInList() ? 1 : 0);
        for (int i = endExclusive - 1; i >= insertionIndex; i--) {
            getEntries().remove(i);
        }
        getEntries().addAll(insertionIndex, customEntries);
        updateEntryListIndexes();
    }

    private Set<ResourceLocation> collectCustomEffectIds() {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof PotionEffectEntryModel effect && !effect.isBaseEffect()) {
                ResourceLocation id = ResourceLocation.tryParse(effect.getValue());
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }
}
