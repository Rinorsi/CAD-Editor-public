package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class AddListEntryEntryModel extends EntryModel {
    private final Component tooltip;
    private final Runnable action;

    public AddListEntryEntryModel(CategoryModel category, Component tooltip) {
        this(category, tooltip, null);
    }

    public AddListEntryEntryModel(CategoryModel category, Component tooltip, Runnable action) {
        super(category);
        this.tooltip = Objects.requireNonNull(tooltip);
        this.action = action;
    }

    public Component getTooltip() {
        return tooltip;
    }

    public Runnable getAction() {
        return action == null ? getCategory()::addEntryInList : action;
    }

    @Override
    public boolean isResetable() {
        return false;
    }

    @Override
    public void apply() {
    }

    @Override
    public Type getType() {
        return Type.ADD_LIST_ENTRY;
    }
}
