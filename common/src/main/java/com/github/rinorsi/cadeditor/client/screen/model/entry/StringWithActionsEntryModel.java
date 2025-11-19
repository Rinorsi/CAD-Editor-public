package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class StringWithActionsEntryModel extends ValueEntryModel<String> {
    private String placeholder;
    private final List<ActionButton> buttons = new ArrayList<>();

    public StringWithActionsEntryModel(CategoryModel category, MutableComponent label, String value, Consumer<String> action) {
        super(category, label, value, action);
    }

    @Override
    public Type getType() {
        return Type.STRING_WITH_ACTIONS;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void addButton(ActionButton button) {
        buttons.add(button);
    }

    public List<ActionButton> getButtons() {
        return Collections.unmodifiableList(buttons);
    }

    public record ActionButton(ResourceLocation icon, MutableComponent tooltip, Runnable action) {
    }
}
