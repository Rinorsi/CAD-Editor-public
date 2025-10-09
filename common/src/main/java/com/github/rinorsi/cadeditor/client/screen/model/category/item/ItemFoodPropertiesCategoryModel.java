package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FoodUsingConvertsToEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;

public class ItemFoodPropertiesCategoryModel extends ItemEditorCategoryModel {
    private final FoodComponentState state;

    public ItemFoodPropertiesCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("food_basics"), editor);
        state = editor.getFoodState();
    }

    @Override
    protected void setupEntries() {
        getEntries().add(new IntegerEntryModel(this, ModTexts.gui("nutrition"), state.getNutrition(), state::setNutrition));
        getEntries().add(new FloatEntryModel(this, ModTexts.gui("saturation"), state.getSaturation(), state::setSaturation));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("always_eat"), state.isAlwaysEat(), state::setAlwaysEat));
        getEntries().add(new FloatEntryModel(this, ModTexts.gui("eat_seconds"), state.getEatSeconds(), state::setEatSeconds));
        getEntries().add(FoodUsingConvertsToEntryModel.create(this, state));

    }

    @Override
    public void apply() {
        super.apply();
        getParent().applyFoodComponent();
    }
}
