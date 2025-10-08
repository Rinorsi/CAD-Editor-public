package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.franckyi.guapi.api.util.Predicates;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class IntegerEntryModel extends NumberEntryModel<Integer> {
    public IntegerEntryModel(CategoryModel category, MutableComponent label, int value, Consumer<Integer> action) {
        super(category, label, value, action, Predicates.IS_INT, i -> Integer.toString(i), Integer::parseInt);
    }

    public IntegerEntryModel(CategoryModel category, MutableComponent label, int value, Consumer<Integer> action, Predicate<Integer> validator) {
        super(category, label, value, action, validator, Predicates.IS_INT, i -> Integer.toString(i), Integer::parseInt);
    }

    @Override
    public double getArrowStepShift() {
        return 5d;
    }

    @Override
    public double getArrowStepCtrl() {
        return 1d;
    }

    @Override
    public Integer offsetValue(Integer current, double delta) {
        long adjusted = Math.round(current + delta);
        if (adjusted > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (adjusted < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) adjusted;
    }
}
