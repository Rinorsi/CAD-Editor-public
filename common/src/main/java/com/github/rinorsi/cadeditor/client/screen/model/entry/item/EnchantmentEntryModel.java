package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.IntegerProperty;
import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemEditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.SelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

public class EnchantmentEntryModel extends SelectionEntryModel {
    private final IntegerProperty levelProperty;
    private final BiConsumer<String, Integer> action;
    protected int defaultLevel;

    public EnchantmentEntryModel(CategoryModel category, String id, int level, BiConsumer<String, Integer> action) {
        super(category, null, id, null);
        levelProperty = IntegerProperty.create(level);
        this.action = action;
        defaultLevel = level;
    }

    public int getLevel() {
        return levelProperty().getValue();
    }

    public IntegerProperty levelProperty() {
        return levelProperty;
    }

    public void setLevel(int value) {
        levelProperty().setValue(value);
    }

    @Override
    public void apply() {
        action.accept(getValue(), getLevel());
        defaultValue = getValue();
        defaultLevel = getLevel();
    }

    @Override
    public void reset() {
        super.reset();
        setLevel(defaultLevel);
    }

    @Override
    public ItemEditorCategoryModel getCategory() {
        return (ItemEditorCategoryModel) super.getCategory();
    }

    @Override
    public Type getType() {
        return Type.ENCHANTMENT;
    }

    @Override
    public List<String> getSuggestions() {
        return ClientCache.getEnchantmentSuggestions();
    }

    @Override
    public MutableComponent getSelectionScreenTitle() {
        return ModTexts.ENCHANTMENT;
    }

    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        ItemStack target = null;
        var ctxParent = getCategory() != null ? getCategory().getParent() : null;
        var ctx = ctxParent != null ? ctxParent.getContext() : null;
        if (ctx != null) target = ctx.getItemStack();

        if (target == null || target.isEmpty()) {
            return ClientCache.getSortedEnchantmentSelectionItems(ItemStack.EMPTY);
        }
        return ClientCache.getSortedEnchantmentSelectionItems(target);
    }
}
