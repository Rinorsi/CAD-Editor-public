package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionEffectEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Editor for the {@code minecraft:death_protection} component.
 */
public class ItemDeathProtectionCategoryModel extends ItemEditorCategoryModel {
    private final List<MobEffectInstance> initialEffects = new ArrayList<>();
    private final List<MobEffectInstance> stagedEffects = new ArrayList<>();
    private List<ConsumeEffect> preservedOtherEffects = List.of();
    private boolean clearStatusEffects;

    public ItemDeathProtectionCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("death_protection"), editor);
    }

    @Override
    protected void setupEntries() {
        loadStateFromStack();
        getEntries().add(new BooleanEntryModel(
                this,
                ModTexts.gui("death_protection_clear_effects"),
                clearStatusEffects,
                value -> clearStatusEffects = value != null && value
        ));
        if (initialEffects.isEmpty()) {
            getEntries().add(createEffectEntry(null));
        } else {
            initialEffects.forEach(effect -> getEntries().add(createEffectEntry(effect)));
        }
    }

    @Override
    public int getEntryListStart() {
        return 1;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createEffectEntry(null);
    }

    @Override
    public int getEntryHeight() {
        return 50;
    }

    @Override
    protected net.minecraft.network.chat.MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.EFFECT;
    }

    @Override
    public void apply() {
        stagedEffects.clear();
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        List<ConsumeEffect> effects = new ArrayList<>();
        if (clearStatusEffects) {
            effects.add(ClearAllStatusEffectsConsumeEffect.INSTANCE);
        }
        if (!stagedEffects.isEmpty()) {
            effects.add(new ApplyStatusEffectsConsumeEffect(List.copyOf(stagedEffects)));
        }
        effects.addAll(preservedOtherEffects);
        if (effects.isEmpty()) {
            stack.remove(DataComponents.DEATH_PROTECTION);
            removeComponentFromData();
        } else {
            stack.set(DataComponents.DEATH_PROTECTION, new DeathProtection(List.copyOf(effects)));
        }
    }

    private void loadStateFromStack() {
        initialEffects.clear();
        preservedOtherEffects = new ArrayList<>();
        clearStatusEffects = false;

        ItemStack stack = getParent().getContext().getItemStack();
        DeathProtection component = stack.get(DataComponents.DEATH_PROTECTION);
        if (component != null) {
            for (ConsumeEffect effect : component.deathEffects()) {
                if (effect instanceof ClearAllStatusEffectsConsumeEffect) {
                    clearStatusEffects = true;
                } else if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
                    apply.effects().forEach(inst -> initialEffects.add(new MobEffectInstance(inst)));
                } else {
                    preservedOtherEffects = appendImmutable(preservedOtherEffects, effect);
                }
            }
        } else {
            clearStatusEffects = true;
        }

        if (initialEffects.isEmpty()) {
            initialEffects.addAll(defaultTotemEffects());
        }
    }

    private EntryModel createEffectEntry(MobEffectInstance effect) {
        if (effect != null) {
            String id = effect.getEffect()
                    .unwrapKey()
                    .map(key -> key.identifier().toString())
                    .orElse("minecraft:empty");
            return new PotionEffectEntryModel(
                    this,
                    id,
                    effect.getAmplifier(),
                    effect.getDuration(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon(),
                    this::collectEffect
            );
        }
        String defaultId = MobEffects.REGENERATION.unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("minecraft:regeneration");
        return new PotionEffectEntryModel(this, defaultId, 1, 900, false, true, true, this::collectEffect);
    }

    private void collectEffect(PotionEffectEntryModel entry) {
        var registryOpt = ClientUtil.registryAccess().lookup(Registries.MOB_EFFECT);
        if (registryOpt.isEmpty()) {
            entry.setValid(false);
            return;
        }
        String value = entry.getValue();
        Identifier id = value == null ? null : Identifier.tryParse(value);
        if (id == null) {
            entry.setValid(false);
            return;
        }
        ResourceKey<net.minecraft.world.effect.MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, id);
        Holder<net.minecraft.world.effect.MobEffect> holder = registryOpt.get().get(key).orElse(null);
        if (holder == null) {
            entry.setValid(false);
            return;
        }
        entry.setValid(true);
        MobEffectInstance instance = new MobEffectInstance(
                holder,
                Math.max(1, entry.getDuration()),
                Math.max(0, entry.getAmplifier()),
                entry.isAmbient(),
                entry.isShowParticles(),
                entry.isShowIcon()
        );
        stagedEffects.add(instance);
    }

    private void removeComponentFromData() {
        getParent().removeComponentFromDataTag("minecraft:death_protection");
    }

    private static List<MobEffectInstance> defaultTotemEffects() {
        List<MobEffectInstance> list = new ArrayList<>();
        list.add(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        list.add(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        list.add(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        return list;
    }

    private static List<ConsumeEffect> appendImmutable(List<ConsumeEffect> original, ConsumeEffect effect) {
        if (original.isEmpty()) {
            return List.of(effect);
        }
        List<ConsumeEffect> copy = new ArrayList<>(original);
        copy.add(effect);
        return copy;
    }
}
