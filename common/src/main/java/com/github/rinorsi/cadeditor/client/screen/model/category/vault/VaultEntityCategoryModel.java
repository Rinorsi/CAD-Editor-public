package com.github.rinorsi.cadeditor.client.screen.model.category.vault;

import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.screen.model.VaultScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.vault.VaultEntityEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;

public class VaultEntityCategoryModel extends VaultCategoryModel {
    public VaultEntityCategoryModel(VaultScreenModel parent) {
        super(ModTexts.ENTITY, parent);
    }

    @Override
    protected void setupEntries() {
        Vault.getInstance().getEntities().forEach(entity -> getEntries().add(new VaultEntityEntryModel(this, entity)));
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    protected boolean canAddEntryInList() {
        return false;
    }
}
