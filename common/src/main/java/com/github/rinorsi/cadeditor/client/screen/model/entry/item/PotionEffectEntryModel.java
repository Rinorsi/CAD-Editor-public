package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.BooleanProperty;
import com.github.franckyi.databindings.api.IntegerProperty;
import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.SelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class PotionEffectEntryModel extends SelectionEntryModel {
    private final IntegerProperty amplifierProperty;
    private final IntegerProperty durationProperty;
    private final BooleanProperty ambientProperty;
    private final BooleanProperty showParticlesProperty;
    private final BooleanProperty showIconProperty;
    private final BooleanProperty useSecondsProperty;
    private final PotionEffectConsumer callback;
    private boolean defaultUseSeconds;
    private final boolean baseEffect;
    private final CompoundTag originalSnapshot;

    public PotionEffectEntryModel(CategoryModel category, String id, int amplifier, int duration, boolean ambient,
                                  boolean showParticles, boolean showIcon, PotionEffectConsumer callback) {
        this(category, id, amplifier, duration, ambient, showParticles, showIcon, callback, false, null);
    }

    public PotionEffectEntryModel(CategoryModel category, String id, int amplifier, int duration, boolean ambient,
                                  boolean showParticles, boolean showIcon, PotionEffectConsumer callback,
                                  boolean baseEffect, CompoundTag originalTag) {
        super(category, null, id, s -> {
        });
        amplifierProperty = IntegerProperty.create(amplifier);
        durationProperty = IntegerProperty.create(duration);
        ambientProperty = BooleanProperty.create(ambient);
        showParticlesProperty = BooleanProperty.create(showParticles);
        showIconProperty = BooleanProperty.create(showIcon);
        boolean useSeconds = duration % 20 == 0;
        useSecondsProperty = BooleanProperty.create(useSeconds);
        this.callback = callback;
        defaultUseSeconds = useSeconds;
        this.baseEffect = baseEffect;
        this.originalSnapshot = originalTag == null ? null : normalizeTag(originalTag);
    }

    @Override
    public void apply() {
        callback.consume(this);
        super.apply();
        defaultUseSeconds = isUseSeconds();
    }

    @Override
    public void reset() {
        super.reset();
        setUseSeconds(defaultUseSeconds);
    }

    public int getAmplifier() {
        return amplifierProperty().getValue();
    }

    public IntegerProperty amplifierProperty() {
        return amplifierProperty;
    }

    public void setAmplifier(int value) {
        amplifierProperty().setValue(value);
    }

    public int getDuration() {
        return durationProperty().getValue();
    }

    public IntegerProperty durationProperty() {
        return durationProperty;
    }

    public void setDuration(int value) {
        durationProperty().setValue(value);
    }

    public boolean isAmbient() {
        return ambientProperty().getValue();
    }

    public BooleanProperty ambientProperty() {
        return ambientProperty;
    }

    public void setAmbient(boolean value) {
        ambientProperty().setValue(value);
    }

    public boolean isShowParticles() {
        return showParticlesProperty().getValue();
    }

    public BooleanProperty showParticlesProperty() {
        return showParticlesProperty;
    }

    public void setShowParticles(boolean value) {
        showParticlesProperty().setValue(value);
    }

    public boolean isShowIcon() {
        return showIconProperty().getValue();
    }

    public BooleanProperty showIconProperty() {
        return showIconProperty;
    }

    public void setShowIcon(boolean value) {
        showIconProperty().setValue(value);
    }

    public boolean isUseSeconds() {
        return useSecondsProperty().getValue();
    }

    public BooleanProperty useSecondsProperty() {
        return useSecondsProperty;
    }

    public void setUseSeconds(boolean value) {
        useSecondsProperty().setValue(value);
    }

    public boolean isBaseEffect() {
        return baseEffect;
    }

    public boolean isModifiedFromOriginal() {
        if (!baseEffect) {
            return true;
        }
        if (originalSnapshot == null) {
            return true;
        }
        return !originalSnapshot.equals(normalizeTag(toCompoundTag()));
    }

    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", getValue());
        tag.putInt("amplifier", getAmplifier());
        tag.putInt("duration", getDuration());
        tag.putBoolean("ambient", isAmbient());
        tag.putBoolean("show_particles", isShowParticles());
        tag.putBoolean("show_icon", isShowIcon());
        return tag;
    }

    private static CompoundTag normalizeTag(CompoundTag tag) {
        CompoundTag normalized = new CompoundTag();
        normalized.putString("id", tag.getString("id"));
        normalized.putInt("amplifier", tag.getInt("amplifier"));
        normalized.putInt("duration", tag.contains("duration", Tag.TAG_INT) ? tag.getInt("duration") : 1);
        normalized.putBoolean("ambient", tag.getBoolean("ambient"));
        boolean showParticles = !tag.contains("show_particles", Tag.TAG_BYTE) || tag.getBoolean("show_particles");
        normalized.putBoolean("show_particles", showParticles);
        boolean showIcon = tag.contains("show_icon", Tag.TAG_BYTE) ? tag.getBoolean("show_icon") : true;
        normalized.putBoolean("show_icon", showIcon);
        return normalized;
    }

    @Override
    public Type getType() {
        return Type.POTION_EFFECT;
    }

    @Override
    public List<String> getSuggestions() {
        return ClientCache.getEffectSuggestions();
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.EFFECTS;
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        return ClientCache.getEffectSelectionItems();
    }

    @FunctionalInterface
    public interface PotionEffectConsumer {
        void consume(PotionEffectEntryModel entry);
    }
}
