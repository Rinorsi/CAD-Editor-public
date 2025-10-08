package com.github.rinorsi.cadeditor.client.screen.mvc;

import com.github.franckyi.guapi.api.mvc.SimpleMVC;
import com.github.rinorsi.cadeditor.client.screen.controller.CategoryController;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.view.CategoryView;

public final class CategoryMVC extends SimpleMVC<CategoryModel, CategoryView, CategoryController> {
    public static final CategoryMVC INSTANCE = new CategoryMVC();

    private CategoryMVC() {
        super(CategoryView::new, CategoryController::new);
    }
}
