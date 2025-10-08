package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.DoubleProperty;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;

public class FoodEffectEntryModel extends PotionEffectEntryModel {
    private final DoubleProperty probabilityProperty;
    private double defaultProbability;
    private final FoodEffectConsumer foodCallback;

    public FoodEffectEntryModel(CategoryModel category, String id, int amplifier, int duration, boolean ambient,
                                boolean showParticles, boolean showIcon, double probability,
                                FoodEffectConsumer callback) {
        super(category, id, amplifier, duration, ambient, showParticles, showIcon, entry -> { });
        probabilityProperty = DoubleProperty.create(probability);
        defaultProbability = probability;
        foodCallback = callback;
    }

    @Override
    public void apply() {
        foodCallback.consume(getValue(), getAmplifier(), getDuration(), isAmbient(), isShowParticles(), isShowIcon(),
                (float) getProbability());
        super.apply();
        defaultProbability = getProbability();
    }

    @Override
    public void reset() {
        super.reset();
        setProbability(defaultProbability);
    }

    @Override
    public Type getType() {
        return Type.FOOD_EFFECT;
    }

    public double getProbability() {
        return probabilityProperty().getValue();
    }

    public void setProbability(double value) {
        probabilityProperty().setValue(value);
    }

    public DoubleProperty probabilityProperty() {
        return probabilityProperty;
    }

    @FunctionalInterface
    public interface FoodEffectConsumer {
        void consume(String id, int amplifier, int duration, boolean ambient, boolean showParticles, boolean showIcon,
                     float probability);
    }
}

