package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemHideFlagsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

@SuppressWarnings("this-escape")
public class HideFlagEntryModel extends BooleanEntryModel {
    private final ItemHideFlagsCategoryModel.HideFlag hideFlag;

    public HideFlagEntryModel(CategoryModel category, ItemHideFlagsCategoryModel.HideFlag hideFlag, boolean value, Consumer<Boolean> action) {
        super(category, getHideFlagLabel(hideFlag), value, action);
        this.hideFlag = hideFlag;
        withWeight(2);
    }

    private static MutableComponent getHideFlagLabel(ItemHideFlagsCategoryModel.HideFlag hideFlag) {
        MutableComponent label = ModTexts.hide(hideFlag.getName());
        if (hideFlag == ItemHideFlagsCategoryModel.HideFlag.OTHER) {
            label.append(text("*"));
        }
        return label;
    }

    public ItemHideFlagsCategoryModel.HideFlag getHideFlag() {
        return hideFlag;
    }

    @Override
    public Type getType() {
        return Type.HIDE_FLAG;
    }

    public void syncValue(boolean value) {
        setValue(value);
        defaultValue = value;
    }
}
