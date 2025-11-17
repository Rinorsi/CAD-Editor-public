package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EnumEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FoodUsingConvertsToEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.SoundEventSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.Arrays;

public class ItemConsumableCategoryModel extends ItemEditorCategoryModel {
    private final FoodComponentState state;
    private SoundEventSelectionEntryModel soundEntry;

    public ItemConsumableCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("consumable_settings"), editor);
        state = editor.getFoodState();
    }

    @Override
    protected void setupEntries() {
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("consumable_enabled"),
                state.isConsumableStandaloneEnabled(), this::setStandaloneEnabled));
        getEntries().add(new FloatEntryModel(this, ModTexts.gui("consume_seconds"),
                state.getConsumeSeconds(), state::setConsumeSeconds));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("consume_particles"),
                state.hasConsumeParticles(), state::setHasConsumeParticles));

        EnumEntryModel<ItemUseAnimation> animationEntry = new EnumEntryModel<>(
                this,
                ModTexts.gui("use_animation"),
                Arrays.asList(ItemUseAnimation.values()),
                state.getAnimation(),
                state::setAnimation
        ).withTextFactory(ModTexts::useAnimationOption);
        getEntries().add(animationEntry);

        String soundId = state.getConsumeSoundId();
        String namespace = soundId.contains(":") ? soundId.substring(0, soundId.indexOf(':')) : "minecraft";
        soundEntry = new SoundEventSelectionEntryModel(
                this,
                ModTexts.gui("consume_sound"),
                soundId,
                this::applyConsumeSoundId,
                "namespace:" + namespace
        );
        getEntries().add(soundEntry);

        getEntries().add(FoodUsingConvertsToEntryModel.create(this, state));
    }

    @Override
    public void apply() {
        super.apply();
        getParent().applyFoodComponent();
    }

    private void setStandaloneEnabled(boolean value) {
        state.setConsumableStandaloneEnabled(value);
        getParent().applyFoodComponent();
    }

    private void applyConsumeSoundId(String id) {
        String sanitized = id == null ? "" : id.trim();
        boolean success = state.setConsumeSoundId(sanitized);
        soundEntry.setValid(success);
    }
}
