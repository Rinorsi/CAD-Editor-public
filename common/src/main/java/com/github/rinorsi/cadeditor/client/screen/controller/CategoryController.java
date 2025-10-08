package com.github.rinorsi.cadeditor.client.screen.controller;

import com.github.franckyi.guapi.api.mvc.AbstractController;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.view.CategoryView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public class CategoryController extends AbstractController<CategoryModel, CategoryView> {
    public CategoryController(CategoryModel model, CategoryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        model.selectedProperty().addListener(this::updateLabel);
        model.validProperty().addListener(this::updateLabel);
        view.getLabel().hoveredProperty().addListener(this::updateLabel);
        view.getLabel().onAction(() -> model.getParent().setSelectedCategory(model));
        view.getLabel().setLabel(model.getName());
        updateLabel();
    }

    private void updateLabel() {
        MutableComponent text = model.getName().plainCopy();
        if (view.getLabel().isHovered()) {
            text.withStyle(ChatFormatting.UNDERLINE);
        }
        if (model.isSelected()) {
            text.withStyle(ChatFormatting.BOLD);
            if (model.isValid()) {
                text.withStyle(ChatFormatting.YELLOW);
            } else {
                text.withStyle(ChatFormatting.RED);
            }
        } else if (!model.isValid()) {
            text.withStyle(ChatFormatting.RED);
        }
        view.getLabel().setLabel(text);
    }
}
