package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FoodEffectEntryModel;
import com.github.rinorsi.cadeditor.client.util.CompatFood;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.FoodComponentState.FoodEffectData;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemFoodEffectsCategoryModel extends ItemEditorCategoryModel {
    private static final int MAX_DURATION_TICKS = 20 * 60 * 60;
    private static final int MAX_AMPLIFIER = 255;

    private final FoodComponentState state;
    private List<FoodEffectData> stagedEffects = List.of();

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

    private EntryModel createFoodEffectEntry(FoodEffectData effect) {
        if (effect != null) {
            MobEffectInstance instance = effect.effect();
            String id = instance.getEffect().unwrapKey()
                    .map(key -> key.identifier().toString())
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
        String defaultId = MobEffects.SPEED.unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("minecraft:speed");
        return new FoodEffectEntryModel(this, defaultId, 0, 1, false, true, true, 1.0f, this::addFoodEffect);
    }

    private FoodEffectEntryModel createFoodEffectEntryFor(Identifier id) {
        var registryOpt = ClientUtil.registryAccess().lookup(Registries.MOB_EFFECT);
        if (registryOpt.isPresent()) {
            var holder = registryOpt.get().get(ResourceKey.create(Registries.MOB_EFFECT, id));
            if (holder.isPresent()) {
                MobEffectInstance instance = new MobEffectInstance(
                        holder.get(),
                        160,
                        0,
                        false,
                        true,
                        true
                );
                return (FoodEffectEntryModel) createFoodEffectEntry(new FoodEffectData(instance, 1.0f));
            }
        }
        FoodEffectEntryModel entry = (FoodEffectEntryModel) createFoodEffectEntry(null);
        entry.setValue(id.toString());
        return entry;
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

        Identifier rl = Identifier.tryParse(id);
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

        List<FoodEffectData> list;
        if (stagedEffects instanceof ArrayList<FoodEffectData> existing) {
            list = existing;
        } else {
            list = new ArrayList<>();
            stagedEffects = list;
        }

        CompatFood.makeApplyEffect(instance, probability).ifPresent(list::add);
    }

    private void openEffectSelection() {
        Set<Identifier> current = collectEffectIds();
        ModScreenHandler.openListSelectionScreen(
                ModTexts.EFFECTS.copy(),
                "",
                ClientCache.getEffectSelectionItems(),
                value -> {},
                true,
                this::applySelectedEffects,
                current
        );
    }

    private void applySelectedEffects(List<Identifier> selected) {
        Map<Identifier, FoodEffectEntryModel> existing = new LinkedHashMap<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof FoodEffectEntryModel effectEntry) {
                Identifier id = Identifier.tryParse(effectEntry.getValue());
                if (id != null) {
                    existing.putIfAbsent(id, effectEntry);
                }
            }
        }
        List<FoodEffectEntryModel> desired = new ArrayList<>();
        if (selected != null) {
            for (Identifier id : selected) {
                FoodEffectEntryModel entry = existing.remove(id);
                if (entry == null) {
                    entry = createFoodEffectEntryFor(id);
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

    private Set<Identifier> collectEffectIds() {
        Set<Identifier> ids = new LinkedHashSet<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof FoodEffectEntryModel effectEntry) {
                Identifier id = Identifier.tryParse(effectEntry.getValue());
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }
}
