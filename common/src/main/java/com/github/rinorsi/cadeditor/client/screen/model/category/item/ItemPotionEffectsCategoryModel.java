package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionEffectEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
    private String selectedPotionId = "";
    private int selectedCustomColor = Color.NONE;

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
                    .flatMap(h -> h.unwrapKey().map(k -> java.util.Optional.of(k.identifier().toString())).orElse(java.util.Optional.empty()))
                    .orElse("");
            customColor = contents.customColor().orElse(Color.NONE);
            originalBaseEffectTags = resolveBasePotionEffects(potionId);
            originalBaseEntryCount = originalBaseEffectTags.size();
            originalBaseEffectTags.forEach(tag -> getEntries().add(createPotionEffectEntry(tag, true)));
            contents.customEffects().forEach(e -> getEntries().add(createPotionEffectEntry(toTag(e), false)));
        } else {
            CompoundTag data = getData();
            CompoundTag legacy = data == null ? null : data.getCompound("tag").orElse(null);
            potionId = NbtHelper.getString(legacy, "Potion", "");
            customColor = NbtHelper.getInt(legacy, "CustomPotionColor", Color.NONE);
            originalBaseEffectTags = resolveBasePotionEffects(potionId);
            originalBaseEntryCount = originalBaseEffectTags.size();
            originalBaseEffectTags.forEach(tag -> getEntries().add(createPotionEffectEntry(tag, true)));
            ListTag customEffects = legacy == null ? new ListTag() : legacy.getListOrEmpty("custom_potion_effects");
            customEffects.stream()
                    .map(CompoundTag.class::cast)
                    .map(t -> createPotionEffectEntry(t, false))
                    .forEach(getEntries()::add);
        }
        selectedPotionId = potionId;
        selectedCustomColor = customColor;
        getEntries().add(0, new PotionSelectionEntryModel(this, ModTexts.DEFAULT_POTION,
                potionId, customColor,
                this::setPotionId, this::setCustomPotionColor));
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
                String id = NbtHelper.getString(c, "id", "");
                Identifier rl = Identifier.tryParse(id);
                if (rl == null) {
                    continue;
                }
                var holderOpt = effectLookup.get(ResourceKey.create(Registries.MOB_EFFECT, rl));
                if (holderOpt.isEmpty()) {
                    continue;
                }
                int amplifier = c.getIntOr("amplifier", 0);
                int duration = c.getIntOr("duration", 1);
                boolean ambient = c.getBooleanOr("ambient", false);
                boolean showParticles = !c.contains("show_particles") || c.getBooleanOr("show_particles", true);
                boolean showIcon = c.getBooleanOr("show_icon", false);
                effects.add(new MobEffectInstance(holderOpt.get(), duration, amplifier, ambient, showParticles, showIcon));
            }
        }
        String potionStr = selectedPotionId == null ? "" : selectedPotionId;
        if (baseEffectsModified && !potionStr.isEmpty()) {
            potionStr = "";
            selectedPotionId = potionStr;
            if (!getEntries().isEmpty() && getEntries().get(0) instanceof PotionSelectionEntryModel selection) {
                selection.setValue(potionStr);
                selection.apply();
            }
        }
        int customColor = getCustomPotionColor();
        PotionContents contents = null;
        if (potionLookupOpt.isPresent()) {
            var potionLookup = potionLookupOpt.get();
            Identifier rl = potionStr.isEmpty() ? Identifier.parse("minecraft:empty") : Identifier.tryParse(potionStr);
            java.util.Optional<Holder<Potion>> pot = java.util.Optional.empty();
            if (!baseEffectsModified && rl != null) {
                var potHolder = potionLookup.get(ResourceKey.create(Registries.POTION, rl));
                if (potHolder.isPresent()) {
                    pot = java.util.Optional.of(potHolder.get());
                }
            }
            contents = new PotionContents(pot,
                    customColor != Color.NONE ? java.util.Optional.of(customColor) : java.util.Optional.empty(),
                    effects,
                    java.util.Optional.empty());
        }
        if (contents != null) {
            stack.set(DataComponents.POTION_CONTENTS, contents);
        } else {
            stack.remove(DataComponents.POTION_CONTENTS);
        }
        CompoundTag data = getData();
        CompoundTag legacy = data == null ? null : data.getCompound("tag").orElse(null);
        if (legacy != null) {
            legacy.remove("Potion");
            legacy.remove("CustomPotionColor");
            legacy.remove("custom_potion_effects");
            if (legacy.isEmpty()) {
                data.remove("tag");
            }
        }
    }

    private int getCustomPotionColor() {
        return selectedCustomColor;
    }

    private void setCustomPotionColor(int color) {
        selectedCustomColor = color;
    }

    private void setPotionId(String potionId) {
        selectedPotionId = potionId == null ? "" : potionId.trim();
    }

    private EntryModel createPotionEffectEntry(CompoundTag tag, boolean baseEffect) {
        if (tag != null) {
            String id = NbtHelper.getString(tag, "id", "");
            int amplifier = tag.getIntOr("amplifier", tag.getIntOr("Amplifier", 0));
            int duration = tag.getIntOr("duration", tag.getIntOr("Duration", 1));
            boolean ambient = tag.getBooleanOr("ambient", tag.getBooleanOr("Ambient", false));
            boolean showParticles = tag.getBoolean("show_particles")
                    .or(() -> tag.getBoolean("ShowParticles"))
                    .orElse(true);
            boolean showIcon = tag.getBoolean("show_icon")
                    .or(() -> tag.getBoolean("ShowIcon"))
                    .orElse(true);
            return new PotionEffectEntryModel(this, id, amplifier, duration, ambient, showParticles, showIcon,
                    this::collectPotionEffect, baseEffect, tag);
        }
        String defaultId = MobEffects.SPEED.unwrapKey()
                .map(key -> key.identifier().toString())
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
        Identifier rl = Identifier.tryParse(potionId);
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
        String id = e.getEffect().unwrapKey().map(k -> k.identifier().toString()).orElse("");
        tag.putString("id", id);
        tag.putInt("amplifier", e.getAmplifier());
        tag.putInt("duration", e.getDuration());
        tag.putBoolean("ambient", e.isAmbient());
        tag.putBoolean("show_particles", e.isVisible());
        tag.putBoolean("show_icon", e.showIcon());
        return tag;
    }

    private void openCustomEffectSelection() {
        Set<Identifier> current = collectCustomEffectIds();
        ModScreenHandler.openListSelectionScreen(ModTexts.EFFECTS.copy(),
                "", ClientCache.getEffectSelectionItems(),
                value -> {}, true,
                this::applyCustomSelection,
                current);
    }

    private void applyCustomSelection(List<Identifier> selected) {
        Map<Identifier, PotionEffectEntryModel> existing = new LinkedHashMap<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof PotionEffectEntryModel effect && !effect.isBaseEffect()) {
                Identifier id = Identifier.tryParse(effect.getValue());
                if (id != null) {
                    existing.putIfAbsent(id, effect);
                }
            }
        }
        List<PotionEffectEntryModel> desired = new ArrayList<>();
        if (selected != null) {
            for (Identifier id : selected) {
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

    private Set<Identifier> collectCustomEffectIds() {
        Set<Identifier> ids = new LinkedHashSet<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof PotionEffectEntryModel effect && !effect.isBaseEffect()) {
                Identifier id = Identifier.tryParse(effect.getValue());
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }
}
