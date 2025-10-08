package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;

public class EntityVillagerTradeCategoryModel extends EntityCategoryModel {
    public EntityVillagerTradeCategoryModel(EntityEditorModel editor) {
        super(ModTexts.VILLAGER_TRADES, editor);
    }

    @Override
    protected void setupEntries() {
        getEntries().add(new ActionEntryModel(this,
                ModTexts.todoPlaceholder(ModTexts.VILLAGER_TRADES),
                () -> ClientUtil.showMessage(ModTexts.Messages.warnNotImplemented(ModTexts.VILLAGER_TRADES))));
    }
}
