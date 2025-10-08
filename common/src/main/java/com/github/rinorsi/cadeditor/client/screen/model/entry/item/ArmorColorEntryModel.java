package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;

import java.util.function.Consumer;

public class ArmorColorEntryModel extends IntegerEntryModel {
    public ArmorColorEntryModel(CategoryModel category, int color, Consumer<Integer> action) {
        super(category, ModTexts.ARMOR_COLOR, color, action);
    }

    @Override
    public Type getType() {
        return Type.ARMOR_COLOR;
    }
}
