package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnumEntryModel<E> extends ValueEntryModel<E> {
    private final Collection<? extends E> values;
    private Function<E, Component> textFactory;

    public EnumEntryModel(CategoryModel category, MutableComponent label, E[] values, E value, Consumer<E> action) {
        this(category, label, Arrays.asList(values), value, action);
    }

    public EnumEntryModel(CategoryModel category, MutableComponent label, Collection<? extends E> values, E value, Consumer<E> action) {
        super(category, label, value, action);
        this.values = values;
    }

    public EnumEntryModel<E> withTextFactory(Function<E, Component> textFactory) {
        this.textFactory = textFactory;
        return this;
    }

    public Function<E, Component> getTextFactory() {
        return textFactory;
    }

    public Collection<? extends E> getValues() {
        return values;
    }

    @Override
    public Type getType() {
        return Type.ENUM;
    }
}
