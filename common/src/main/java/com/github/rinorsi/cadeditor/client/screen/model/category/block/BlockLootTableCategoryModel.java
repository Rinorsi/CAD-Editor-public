package com.github.rinorsi.cadeditor.client.screen.model.category.block;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.BlockEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;

public class BlockLootTableCategoryModel extends BlockEditorCategoryModel {
    public BlockLootTableCategoryModel(BlockEditorModel editor) {
        super(ModTexts.LOOT_TABLE, editor);
    }

    @Override
    protected void setupEntries() {
        getEntries().add(new ActionEntryModel(this,
                ModTexts.todoPlaceholder(ModTexts.LOOT_TABLE),
                () -> ClientUtil.showMessage(ModTexts.Messages.warnNotImplemented(ModTexts.LOOT_TABLE))));
    }
}
