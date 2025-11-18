package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FoodEffectEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.FoodComponentState.FoodEffectData;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemFoodEffectsCategoryModel extends ItemEditorCategoryModel {
    private static final int MAX_DURATION_TICKS = 20 * 60 * 60;
    private static final int MAX_AMPLIFIER = 255;
    private static final int MULTI_SELECT_DEFAULT_DURATION = 160;
    private static final float MULTI_SELECT_DEFAULT_PROBABILITY = 1.0f;

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
        Set<ResourceLocation> current = collectEffectIds();
        ModScreenHandler.openListSelectionScreen(ModTexts.EFFECTS.copy(),
                "",
                ClientCache.getEffectSelectionItems(),
                selection -> { },
                true,
                this::applyEffectSelection,
                current);
    }

    private void applyEffectSelection(List<ResourceLocation> selected) {
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
        if (selected != null) {
            for (ResourceLocation id : selected) {
                FoodEffectEntryModel entry = existing.remove(id);
                if (entry == null) {
                    entry = createDefaultEffectEntry(id);
                }
                desired.add(entry);
            }
        }
        replaceEffectEntries(desired);
    }

    private FoodEffectEntryModel createDefaultEffectEntry(ResourceLocation id) {
        FoodEffectEntryModel entry = (FoodEffectEntryModel) createFoodEffectEntry(null);
        if (id != null) {
            entry.setValue(id.toString());
        }
        entry.setAmplifier(0);
        entry.setDuration(MULTI_SELECT_DEFAULT_DURATION);
        entry.setProbability(MULTI_SELECT_DEFAULT_PROBABILITY);
        entry.setAmbient(false);
        entry.setShowParticles(true);
        entry.setShowIcon(true);
        return entry;
    }

    private void replaceEffectEntries(List<FoodEffectEntryModel> desired) {
        int endExclusive = getEntries().size() - (canAddEntryInList() ? 1 : 0);
        for (int i = endExclusive - 1; i >= getEntryListStart(); i--) {
            EntryModel entry = getEntries().get(i);
            if (entry instanceof FoodEffectEntryModel) {
                getEntries().remove(i);
            }
        }
        getEntries().addAll(getEntryListStart(), desired);
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
