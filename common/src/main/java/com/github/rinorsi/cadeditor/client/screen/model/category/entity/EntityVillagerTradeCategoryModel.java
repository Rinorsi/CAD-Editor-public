package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTradeEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTradeItemsEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTradeValuesEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.entity.VillagerTradeItemsEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

public class EntityVillagerTradeCategoryModel extends EntityCategoryModel {
    private static final String OFFERS_TAG = "Offers";
    private static final String RECIPES_TAG = "Recipes";
    private static final String VILLAGER_DATA_TAG = "VillagerData";
    private static final String ASSIGN_PROFESSION_WHEN_SPAWNED_TAG = "AssignProfessionWhenSpawned";
    private static final String DEFAULT_VILLAGER_TYPE = "minecraft:plains";
    private static final String DEFAULT_TRADING_PROFESSION = "minecraft:farmer";
    private static final String NONE_PROFESSION = "minecraft:none";

    private int tradeListStartIndex = -1;
    private CompoundTag offersExtra = new CompoundTag();
    private final List<VillagerTradeEntryModel> tradeEntries = new ArrayList<>();

    public EntityVillagerTradeCategoryModel(EntityEditorModel editor) {
        super(ModTexts.VILLAGER_TRADES, editor);
    }

    @Override
    protected void setupEntries() {
        tradeListStartIndex = 0;
        tradeEntries.clear();
        CompoundTag data = getData();
        CompoundTag offers = data.getCompound(OFFERS_TAG)
                .map(CompoundTag::copy)
                .orElseGet(CompoundTag::new);
        offersExtra = offers.copy();
        offersExtra.remove(RECIPES_TAG);

        ListTag recipes = offers.getList(RECIPES_TAG).orElseGet(ListTag::new);
        for (int i = 0; i < recipes.size(); i++) {
            CompoundTag recipe = recipes.getCompound(i).orElse(null);
            if (recipe == null) {
                continue;
            }
            VillagerTradeEntryModel trade = new VillagerTradeEntryModel(this, recipe);
            tradeEntries.add(trade);
            appendTradeEntries(trade);
        }
    }

    @Override
    public int getEntryListStart() {
        return tradeListStartIndex;
    }

    @Override
    protected boolean canAddEntryInList() {
        return true;
    }

    @Override
    protected MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.TRADE_ADD;
    }

    @Override
    public int getEntryHeight() {
        return VillagerTradeItemsEntryView.ENTRY_HEIGHT;
    }

    @Override
    public void apply() {
        super.apply();
        writeOffers();
    }

    @Override
    public void addEntryInList() {
        VillagerTradeEntryModel trade = new VillagerTradeEntryModel(this, null);
        tradeEntries.add(trade);
        insertTradeEntries(tradeEntries.size() - 1, trade);
        updateEntryListIndexes();
    }

    @Override
    public void moveEntryUp(int index) {
        if (index <= 0 || index >= tradeEntries.size()) {
            return;
        }
        swapTrades(index, index - 1);
    }

    @Override
    public void moveEntryDown(int index) {
        if (index < 0 || index >= tradeEntries.size() - 1) {
            return;
        }
        swapTrades(index, index + 1);
    }

    @Override
    public void deleteEntry(int index) {
        if (index < 0 || index >= tradeEntries.size()) {
            return;
        }
        int baseIndex = getTradeItemsIndex(index);
        getEntries().remove(baseIndex + 1);
        getEntries().remove(baseIndex);
        tradeEntries.remove(index);
        updateEntryListIndexes();
    }

    @Override
    public void updateEntryListIndexes() {
        int size = tradeEntries.size();
        int entryCount = getEntries().size();
        for (int i = 0; i < size; i++) {
            VillagerTradeEntryModel trade = tradeEntries.get(i);
            trade.setListSize(size);
            trade.setListIndex(i);
            int baseIndex = getTradeItemsIndex(i);
            if (entryCount <= baseIndex + 1) {
                // entries still being appended (items added but values not yet), wait for next update
                return;
            }
            EntryModel itemsEntry = getEntries().get(baseIndex);
            EntryModel valuesEntry = getEntries().get(baseIndex + 1);
            itemsEntry.setListSize(size);
            itemsEntry.setListIndex(i);
            valuesEntry.setListSize(size);
            valuesEntry.setListIndex(i);
        }
    }

    private void writeOffers() {
        ListTag recipeList = new ListTag();
        for (VillagerTradeEntryModel trade : tradeEntries) {
            if (!trade.getResultItem().isEmpty() && trade.isValid()) {
                recipeList.add(trade.toCompoundTag());
            }
        }

        if (!recipeList.isEmpty() || !offersExtra.isEmpty()) {
            CompoundTag offers = offersExtra.copy();
            if (!recipeList.isEmpty()) {
                offers.put(RECIPES_TAG, recipeList);
            } else {
                offers.remove(RECIPES_TAG);
            }
            getData().put(OFFERS_TAG, offers);
        } else {
            getData().remove(OFFERS_TAG);
        }

        ensureVillagerTradeMetadata(!recipeList.isEmpty());
    }

    private void ensureVillagerTradeMetadata(boolean hasCustomTrades) {
        if (!hasCustomTrades) {
            return;
        }
        CompoundTag data = getData();
        CompoundTag villagerData = data.getCompound(VILLAGER_DATA_TAG).orElseGet(CompoundTag::new);
        String profession = normalizeId(villagerData.getStringOr("profession", ""), NONE_PROFESSION);
        if (NONE_PROFESSION.equals(profession)) {
            villagerData.putString("profession", DEFAULT_TRADING_PROFESSION);
        } else {
            villagerData.putString("profession", profession);
        }
        villagerData.putString("type", normalizeId(villagerData.getStringOr("type", ""), DEFAULT_VILLAGER_TYPE));
        villagerData.putInt("level", clampLevel(villagerData.getIntOr("level", 1)));
        data.put(VILLAGER_DATA_TAG, villagerData);
        // Prevent spawn-time AI from replacing the profession we just forced for custom trades.
        data.putBoolean(ASSIGN_PROFESSION_WHEN_SPAWNED_TAG, false);
    }

    private static String normalizeId(String value, String defaultValue) {
        String result = value == null || value.isBlank() ? defaultValue : value;
        if (!result.contains(":")) {
            result = "minecraft:" + result;
        }
        return result;
    }

    private static int clampLevel(int level) {
        if (level < 1) {
            return 1;
        }
        if (level > 5) {
            return 5;
        }
        return level;
    }
    private void appendTradeEntries(VillagerTradeEntryModel trade) {
        getEntries().add(new VillagerTradeItemsEntryModel(trade));
        getEntries().add(new VillagerTradeValuesEntryModel(trade));
    }

    private void insertTradeEntries(int tradeIndex, VillagerTradeEntryModel trade) {
        int baseIndex = getTradeItemsIndex(tradeIndex);
        int size = getEntries().size();
        if (canAddEntryInList() && size > 0) {
            EntryModel last = getEntries().get(size - 1);
            if (last instanceof com.github.rinorsi.cadeditor.client.screen.model.entry.AddListEntryEntryModel && baseIndex >= size - 1) {
                baseIndex = size - 1;
            }
        }
        getEntries().add(baseIndex, new VillagerTradeItemsEntryModel(trade));
        getEntries().add(baseIndex + 1, new VillagerTradeValuesEntryModel(trade));
    }

    private void swapTrades(int from, int to) {
        if (from == to) {
            return;
        }
        EntryModel fromItems = getEntries().get(getTradeItemsIndex(from));
        EntryModel fromValues = getEntries().get(getTradeItemsIndex(from) + 1);
        EntryModel toItems = getEntries().get(getTradeItemsIndex(to));
        EntryModel toValues = getEntries().get(getTradeItemsIndex(to) + 1);

        getEntries().set(getTradeItemsIndex(from), toItems);
        getEntries().set(getTradeItemsIndex(from) + 1, toValues);
        getEntries().set(getTradeItemsIndex(to), fromItems);
        getEntries().set(getTradeItemsIndex(to) + 1, fromValues);

        VillagerTradeEntryModel tradeFrom = tradeEntries.get(from);
        tradeEntries.set(from, tradeEntries.get(to));
        tradeEntries.set(to, tradeFrom);

        updateEntryListIndexes();
    }

    private int getTradeItemsIndex(int tradeIndex) {
        return tradeListStartIndex + tradeIndex * 2;
    }
}
