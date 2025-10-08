package com.github.rinorsi.cadeditor.client.screen.model.category.config;

import com.github.rinorsi.cadeditor.client.screen.model.ConfigEditorScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import net.minecraft.network.chat.Component;

public abstract class ConfigCategoryModel extends CategoryModel {
    public ConfigCategoryModel(Component name, ConfigEditorScreenModel editor) {
        super(name, editor);
    }
}
