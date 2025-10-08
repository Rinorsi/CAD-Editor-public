package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.screen.model.category.config.ClientConfigCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.config.CommonConfigCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.config.ConfigCategoryModel;

public class ConfigEditorScreenModel extends CategoryEntryScreenModel<ConfigCategoryModel> {
    @Override
    protected void setupCategories() {
        getCategories().addAll(
                new ClientConfigCategoryModel(this),
                new CommonConfigCategoryModel(this)
        );
    }

    @Override
    public void apply() {
        getCategories().forEach(ConfigCategoryModel::apply);
    }
}
