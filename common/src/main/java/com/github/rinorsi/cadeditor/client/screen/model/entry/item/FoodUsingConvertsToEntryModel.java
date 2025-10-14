package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.event.ObservableValueChangeListener;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.FoodComponentState;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ItemSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class FoodUsingConvertsToEntryModel extends ItemSelectionEntryModel {
    private final ObjectProperty<ItemStack> previewStackProperty;
    private final FoodComponentState state;

    public static FoodUsingConvertsToEntryModel create(CategoryModel category, FoodComponentState state) {
        FoodUsingConvertsToEntryModel model = new FoodUsingConvertsToEntryModel(category, state);
        model.initialize();
        return model;
    }

    private FoodUsingConvertsToEntryModel(CategoryModel category, FoodComponentState state) {
        super(category, ModTexts.gui("using_converts_to"), state.getUsingConvertsToId(), state::setUsingConvertsToId);
        this.state = state;
        previewStackProperty = ObjectProperty.create(ItemStack.EMPTY);
    }

    private void initialize() {
        valueProperty().addListener(new UsingConvertsToListener(state, previewStackProperty));
        refreshPreviewFromState();
    }

    @Override
    public EntryModel.Type getType() {
        return EntryModel.Type.FOOD_USING_CONVERTS_TO;
    }

    public ObjectProperty<ItemStack> previewStackProperty() {
        return previewStackProperty;
    }

    public ItemStack getPreviewStack() {
        return previewStackProperty().getValue();
    }

    public void refreshPreviewFromState() {
        updatePreviewFromState(state, previewStackProperty);
    }

    public Optional<ItemStack> getEditableStack() {
        return state.getUsingConvertsToEditorStack();
    }

    public void useStack(ItemStack stack) {
        state.useCustomUsingConvertsTo(stack);
        if (!Optional.ofNullable(getValue()).orElse("").equals(state.getUsingConvertsToId())) {
            setValue(state.getUsingConvertsToId());
        } else {
            refreshPreviewFromState();
        }
    }

    public void clearStack() {
        state.useCustomUsingConvertsTo(ItemStack.EMPTY);
        setValue(state.getUsingConvertsToId());
    }

    private static void updatePreviewFromState(FoodComponentState state, ObjectProperty<ItemStack> previewProperty) {
        ItemStack stack = state.getUsingConvertsToPreview()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
        previewProperty.setValue(stack);
    }

    private static final class UsingConvertsToListener implements ObservableValueChangeListener<String> {
        private final FoodComponentState state;
        private final ObjectProperty<ItemStack> previewProperty;

        private UsingConvertsToListener(FoodComponentState state, ObjectProperty<ItemStack> previewProperty) {
            this.state = state;
            this.previewProperty = previewProperty;
        }

        @Override
        public void onValueChange(String oldValue, String newValue) {
            state.setUsingConvertsToId(newValue);
            updatePreviewFromState(state, previewProperty);
        }
    }
}
