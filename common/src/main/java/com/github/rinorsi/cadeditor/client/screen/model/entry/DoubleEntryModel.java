package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Numeric entry for double precision values.
 */
public class DoubleEntryModel extends NumberEntryModel<Double> {
    public DoubleEntryModel(CategoryModel category, MutableComponent label, double value, Consumer<Double> action) {
        super(category, label, value, action, s -> {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }, d -> Double.toString(d), Double::parseDouble);
    }

    public DoubleEntryModel(CategoryModel category, MutableComponent label, double value, Consumer<Double> action, Predicate<Double> validator) {
        super(category, label, value, action, validator, s -> {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }, d -> Double.toString(d), Double::parseDouble);
    }

    @Override
    public Double offsetValue(Double current, double delta) {
        return current + delta;
    }
}
