package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.MutableComponent;

@SuppressWarnings("this-escape")
public class InfoEntryModel extends EntryModel {
    private final MutableComponent text;

    public InfoEntryModel(CategoryModel category, MutableComponent text) {
        super(category);
        this.text = text;
        setReorderable(false);
    }

    public MutableComponent getText() {
        return text;
    }

    @Override
    public void apply() {
        // informational only
    }

    @Override
    public boolean isDeletable() {
        return false;
    }

    @Override
    public boolean isResetable() {
        return false;
    }

    @Override
    public Type getType() {
        return Type.INFO;
    }
}
