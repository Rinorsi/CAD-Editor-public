package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.franckyi.guapi.api.util.Predicates;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.MutableComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FloatEntryModel extends NumberEntryModel<Float> {
    public FloatEntryModel(CategoryModel category, MutableComponent label, float value, Consumer<Float> action) {
        super(category, label, value, action, Predicates.IS_FLOAT, i -> Float.toString(i), Float::parseFloat);
    }

    public FloatEntryModel(CategoryModel category, MutableComponent label, float value, Consumer<Float> action, Predicate<Float> validator) {
        super(category, label, value, action, validator, Predicates.IS_FLOAT, i -> Float.toString(i), Float::parseFloat);
    }

    @Override
    public double getArrowStep() {
        return 0.1d;
    }

    @Override
    public double getArrowStepShift() {
        return 0.5d;
    }

    @Override
    public double getArrowStepCtrl() {
        return 0.01d;
    }

    @Override
    public Float offsetValue(Float current, double delta) {
        BigDecimal base = BigDecimal.valueOf(current.doubleValue());
        BigDecimal change = BigDecimal.valueOf(delta);
        BigDecimal result = base.add(change).setScale(4, RoundingMode.HALF_UP);
        return result.floatValue();
    }
}
