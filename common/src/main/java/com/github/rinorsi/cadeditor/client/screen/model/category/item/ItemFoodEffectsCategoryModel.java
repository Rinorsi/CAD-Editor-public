package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FoodEffectEntryModel;
import com.github.rinorsi.cadeditor.client.util.CompatFood;
import com.github.rinorsi.cadeditor.common.ModTexts;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemFoodEffectsCategoryModel extends ItemEditorCategoryModel {
    private static final int MAX_DURATION_TICKS = 20 * 60 * 60;
    private static final int MAX_AMPLIFIER = 255;
    private static final int DEFAULT_SELECTION_DURATION = 160;
    private static final float DEFAULT_SELECTION_PROBABILITY = 1.0f;

    private final FoodComponentState state;
    private List<FoodProperties.PossibleEffect> stagedEffects = List.of();

    public ItemFoodEffectsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("food_effects"), editor);
        state = editor.getFoodState();
    }

    @Override
    protected void setupEntries() {
        state.getEffects().forEach(effect -> getEntries().add(createFoodEffectEntry(effect)));
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createFoodEffectEntry(null);
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
    protected MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.EFFECT;
    }

    @Override
    public void apply() {
        if (!state.isEnabled()) {
            stagedEffects = List.of();
            return;
        }
        stagedEffects = new ArrayList<>();
        super.apply();
        state.setEffects(stagedEffects);
        getParent().applyFoodComponent();
    }

    private EntryModel createFoodEffectEntry(FoodProperties.PossibleEffect effect) {
        if (effect != null) {
            MobEffectInstance instance = effect.effect();
            String id = instance.getEffect().unwrapKey()
                    .map(key -> key.location().toString())
                    .orElse("minecraft:empty");
            return new FoodEffectEntryModel(this,
                    id,
                    instance.getAmplifier(),
                    instance.getDuration(),
                    instance.isAmbient(),
                    instance.isVisible(),
                    instance.showIcon(),
                    effect.probability(),
                    this::addFoodEffect);
        }
        String defaultId = MobEffects.MOVEMENT_SPEED.unwrapKey()
                .map(key -> key.location().toString())
                .orElse("minecraft:movement_speed");
        return new FoodEffectEntryModel(this, defaultId, 0, 1, false, true, true, 1.0f, this::addFoodEffect);
    }

    private void addFoodEffect(String id,
                               int amplifier,
                               int duration,
                               boolean ambient,
                               boolean showParticles,
                               boolean showIcon,
                               float probability) {
        var registryOpt = ClientUtil.registryAccess().lookup(Registries.MOB_EFFECT);
        if (registryOpt.isEmpty()) return;

        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return;

        Holder<MobEffect> holder = registryOpt.get()
                .get(ResourceKey.create(Registries.MOB_EFFECT, rl))
                .orElse(null);
        if (holder == null) return;

        int clampedDuration = Math.max(1, Math.min(duration, MAX_DURATION_TICKS));
        int clampedAmplifier = Math.max(0, Math.min(amplifier, MAX_AMPLIFIER));

        MobEffectInstance instance = new MobEffectInstance(
                holder,
                clampedDuration,
                clampedAmplifier,
                ambient,
                showParticles,
                showIcon
        );

        List<FoodProperties.PossibleEffect> list;
        if (stagedEffects instanceof ArrayList<FoodProperties.PossibleEffect> existing) {
            list = existing;
        } else {
            list = new ArrayList<>();
            stagedEffects = list;
        }

        CompatFood.makePossibleEffect(instance, probability).ifPresent(list::add);
    }

    private void openEffectSelection() {
        Set<ResourceLocation> selected = collectEffectIds();
        ModScreenHandler.openListSelectionScreen(ModTexts.EFFECTS.copy(),
                "", ClientCache.getEffectSelectionItems(),
                value -> {}, true,
                this::applySelectedEffects,
                selected);
    }

    private void applySelectedEffects(List<ResourceLocation> selections) {
        Map<ResourceLocation, FoodEffectEntryModel> existing = new LinkedHashMap<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof FoodEffectEntryModel effect) {
                ResourceLocation id = ResourceLocation.tryParse(effect.getValue());
                if (id != null) {
                    existing.putIfAbsent(id, effect);
                }
            }
        }
        List<FoodEffectEntryModel> desired = new ArrayList<>();
        if (selections != null) {
            for (ResourceLocation id : selections) {
                FoodEffectEntryModel entry = existing.remove(id);
                if (entry == null) {
                    entry = (FoodEffectEntryModel) createFoodEffectEntry(null);
                    entry.setValue(id.toString());
                    entry.setDuration(DEFAULT_SELECTION_DURATION);
                    entry.setUseSeconds(true);
                    entry.setProbability(DEFAULT_SELECTION_PROBABILITY);
                    entry.setAmplifier(0);
                    entry.setAmbient(false);
                    entry.setShowParticles(true);
                    entry.setShowIcon(true);
                }
                desired.add(entry);
            }
        }
        replaceEffectEntries(desired);
    }

    private void replaceEffectEntries(List<FoodEffectEntryModel> entries) {
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
            if (entry instanceof FoodEffectEntryModel effect) {
                ResourceLocation id = ResourceLocation.tryParse(effect.getValue());
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }
}
