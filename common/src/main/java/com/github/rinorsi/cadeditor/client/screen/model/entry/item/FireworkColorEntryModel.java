package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class FireworkColorEntryModel extends IntegerEntryModel {
    private final Runnable removeAction;

    public FireworkColorEntryModel(CategoryModel category, MutableComponent label, int color,
                                   Consumer<Integer> action, Runnable removeAction) {
        super(category, label, color, action, value -> true);
        this.removeAction = removeAction;
    }

    public void remove() {
        if (removeAction != null) {
            removeAction.run();
        }
    }

    public boolean hasCustomColor() {
        return getValue() != Color.NONE;
    }

    @Override
    public Type getType() {
        return Type.FIREWORK_COLOR;
    }
}
