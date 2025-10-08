package com.github.rinorsi.cadeditor.client.screen.view;

import com.github.franckyi.guapi.api.node.ListView;
import com.github.franckyi.guapi.api.node.Node;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.mvc.CategoryMVC;
import com.github.rinorsi.cadeditor.client.screen.mvc.EntryMVC;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public abstract class CategoryEntryScreenView extends ScreenView {
    private ListView<CategoryModel> categoryList;
    private ListView<EntryModel> entryList;

    @Override
    protected Node createEditor() {
        return hBox(editor -> {
            editor.add(categoryList = listView(CategoryModel.class, left -> left.itemHeight(20).padding(5).renderer(item -> mvc(CategoryMVC.INSTANCE, item))), 1);
            editor.add(entryList = listView(EntryModel.class, right -> right.itemHeight(25).padding(5).renderer(item -> mvc(EntryMVC.INSTANCE, item))), 2);
            editor.spacing(10).fillHeight();
        });
    }

    public ListView<CategoryModel> getCategoryList() {
        return categoryList;
    }

    public ListView<EntryModel> getEntryList() {
        return entryList;
    }
}
