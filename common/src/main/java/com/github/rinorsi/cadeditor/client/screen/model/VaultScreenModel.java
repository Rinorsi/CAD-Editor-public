package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.screen.model.category.vault.VaultCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.vault.VaultEntityCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.vault.VaultItemCategoryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;

public class VaultScreenModel extends CategoryEntryScreenModel<VaultCategoryModel> {
    @Override
    protected void setupCategories() {
        getCategories().addAll(
                new VaultItemCategoryModel(this),
                new VaultEntityCategoryModel(this)
        );
    }

    @Override
    public void apply() {
        Vault.getInstance().clear();
        getCategories().forEach(VaultCategoryModel::apply);
        Vault.save();
        ClientUtil.showMessage(ModTexts.Messages.successUpdate(ModTexts.VAULT));
    }
}
