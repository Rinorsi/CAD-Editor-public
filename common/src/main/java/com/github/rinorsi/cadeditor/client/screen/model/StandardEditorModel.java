package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.context.EditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import com.github.rinorsi.cadeditor.common.EditorType;

public abstract class StandardEditorModel extends CategoryEntryScreenModel<EditorCategoryModel> implements EditorModel {
    private final EditorContext<?> context;

    public StandardEditorModel(EditorContext<?> context) {
        this.context = context;
    }

    @Override
    public void apply() {
        java.util.List<EditorCategoryModel> snapshot = java.util.List.copyOf(getCategories());
        snapshot.forEach(EditorCategoryModel::apply);
    }

    @Override
    public void update() {
        EditorModel.super.update();
    }

    @Override
    public EditorContext<?> getContext() {
        return context;
    }

    @Override
    public void save() {
        EditorModel.super.save();
        ModScreenHandler.openEditor(EditorType.STANDARD, getContext(), true);
    }
}
