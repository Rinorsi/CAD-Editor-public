package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.context.BlockEditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.category.block.BlockContainerCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.block.BlockContainerLootCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.block.BlockContainerGridCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.block.BlockEntityDataCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.block.BlockSpawnerCategoryModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import com.github.rinorsi.cadeditor.client.screen.model.category.block.BlockStateCategoryModel;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

public class BlockEditorModel extends StandardEditorModel {
    public BlockEditorModel(BlockEditorContext context) {
        super(context);
    }

    @Override
    public BlockEditorContext getContext() {
        return (BlockEditorContext) super.getContext();
    }

    @Override
    protected void setupCategories() {
        if (!getContext().getBlockState().getProperties().isEmpty()) {
            getCategories().add(new BlockStateCategoryModel(this));
        }
        if (isSpawnerBlock()) {
            getCategories().add(new BlockSpawnerCategoryModel(this));
        }
        if (getContext().getBlockEntity() instanceof BaseContainerBlockEntity) {
            getCategories().add(new BlockContainerCategoryModel(this));
            getCategories().add(new BlockContainerGridCategoryModel(this));
            getCategories().add(new BlockContainerLootCategoryModel(this));
        }
        // getCategories().add(new BlockLootTableCategoryModel(this));
        if (getContext().getBlockEntity() != null) {
            getCategories().add(new BlockEntityDataCategoryModel(this));
        }
    }

    private boolean isSpawnerBlock() {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(getContext().getBlockState().getBlock());
        return blockId != null && "minecraft".equals(blockId.getNamespace()) && "spawner".equals(blockId.getPath());
    }
}
