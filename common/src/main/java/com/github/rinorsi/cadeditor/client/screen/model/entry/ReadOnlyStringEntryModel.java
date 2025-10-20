package com.github.rinorsi.cadeditor.client.screen.model.entry;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.MutableComponent;

/**
 * Simple string entry that only displays a value (no editing or reset).
 */
public class ReadOnlyStringEntryModel extends StringEntryModel {
    public ReadOnlyStringEntryModel(CategoryModel category, MutableComponent label, String value) {
        super(category, label, value, v -> {});
    }

    @Override
    public boolean isResetable() {
        return false;
    }
}
