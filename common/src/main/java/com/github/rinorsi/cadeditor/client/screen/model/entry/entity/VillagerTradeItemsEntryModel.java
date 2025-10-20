package com.github.rinorsi.cadeditor.client.screen.model.entry.entity;

import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;

@SuppressWarnings("this-escape")
public class VillagerTradeItemsEntryModel extends EntryModel {
    private final VillagerTradeEntryModel tradeModel;

    public VillagerTradeItemsEntryModel(VillagerTradeEntryModel tradeModel) {
        super(tradeModel.getCategory());
        this.tradeModel = tradeModel;
        super.setValid(tradeModel.isValid());
        tradeModel.validProperty().addListener(this::syncValidFromTrade);
    }

    public VillagerTradeEntryModel getTradeModel() {
        return tradeModel;
    }

    @Override
    public void apply() {
        tradeModel.apply();
    }

    @Override
    public void reset() {
        tradeModel.reset();
    }

    @Override
    public boolean isResetable() {
        return tradeModel.isResetable();
    }

    @Override
    public boolean isDeletable() {
        return tradeModel.isDeletable();
    }

    @Override
    public void setListIndex(int value) {
        super.setListIndex(value);
        tradeModel.setListIndex(value);
    }

    @Override
    public void setListSize(int value) {
        super.setListSize(value);
        tradeModel.setListSize(value);
    }

    @Override
    public void setValid(boolean value) {
        tradeModel.setValid(value);
        super.setValid(value);
    }

    private void syncValidFromTrade(boolean value) {
        super.setValid(value);
    }

    @Override
    public Type getType() {
        return Type.VILLAGER_TRADE_ITEMS;
    }
}
