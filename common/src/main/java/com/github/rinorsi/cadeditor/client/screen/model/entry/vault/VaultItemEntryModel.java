package com.github.rinorsi.cadeditor.client.screen.model.entry.vault;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.screen.model.category.vault.VaultItemCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class VaultItemEntryModel extends EntryModel {
    private final ObjectProperty<ItemStack> itemStackProperty;

    public VaultItemEntryModel(VaultItemCategoryModel parent, ItemStack itemStack) {
        super(parent);
        itemStackProperty = ObjectProperty.create(itemStack);
    }

    public ItemStack getItemStack() {
        return itemStackProperty().getValue();
    }

    public ObjectProperty<ItemStack> itemStackProperty() {
        return itemStackProperty;
    }

    public void setItemStack(ItemStack value) {
        itemStackProperty().setValue(value);
    }

    @Override
    public void apply() {
        CompoundTag tag = ClientUtil.saveItemStack(ClientUtil.registryAccess(), getItemStack());
        Vault.getInstance().saveItem(tag);
    }

    @Override
    public Type getType() {
        return Type.VAULT_ITEM;
    }
}
