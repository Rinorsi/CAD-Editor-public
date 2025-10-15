package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.OminousBottleAmplifier;

public class ItemOminousBottleCategoryModel extends ItemEditorCategoryModel {
    private static final int MIN_AMPLIFIER = 0;
    private static final int MAX_AMPLIFIER = 4;

    private int amplifier;
    private IntegerEntryModel amplifierEntry;

    public ItemOminousBottleCategoryModel(ItemEditorModel editor) {
        super(ModTexts.OMINOUS_BOTTLE, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        OminousBottleAmplifier component = stack.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        amplifier = component != null ? component.value() : MIN_AMPLIFIER;
        amplifierEntry = new IntegerEntryModel(this, ModTexts.OMINOUS_BOTTLE_AMPLIFIER, amplifier,
                value -> amplifier = value == null ? MIN_AMPLIFIER : value,
                value -> value != null && value >= MIN_AMPLIFIER && value <= MAX_AMPLIFIER);
        getEntries().add(amplifierEntry);
        //TODO 这里要补上等级提示，可以看到对应的袭击威胁有多大
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        int sanitized = Math.max(MIN_AMPLIFIER, Math.min(MAX_AMPLIFIER, amplifier));
        amplifier = sanitized;
        if (sanitized <= MIN_AMPLIFIER) {
            stack.remove(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        } else {
            stack.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(sanitized));
        }
    }
}
