package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotDecorationEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.PotDecorations;

import java.util.Optional;

public class ItemPotDecorationsCategoryModel extends ItemEditorCategoryModel {

    private PotDecorationEntryModel backEntry;
    private PotDecorationEntryModel leftEntry;
    private PotDecorationEntryModel rightEntry;
    private PotDecorationEntryModel frontEntry;

    public ItemPotDecorationsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.POT_DECORATIONS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        PotDecorations decorations = stack.get(DataComponents.POT_DECORATIONS);
        if (decorations == null) {
            decorations = PotDecorations.EMPTY;
        }
        backEntry = createEntry(ModTexts.POT_BACK, decorations.back());
        leftEntry = createEntry(ModTexts.POT_LEFT, decorations.left());
        rightEntry = createEntry(ModTexts.POT_RIGHT, decorations.right());
        frontEntry = createEntry(ModTexts.POT_FRONT, decorations.front());

        getEntries().add(backEntry);
        getEntries().add(leftEntry);
        getEntries().add(rightEntry);
        getEntries().add(frontEntry);
    }

    private PotDecorationEntryModel createEntry(net.minecraft.network.chat.MutableComponent label, Optional<Item> optionalItem) {
        ItemStack stack = optionalItem.map(ItemStack::new).orElse(ItemStack.EMPTY);
        return new PotDecorationEntryModel(this, label, stack);
    }

    @Override
    public void apply() {
        super.apply();

        Optional<Item> back = toItem(backEntry);
        Optional<Item> left = toItem(leftEntry);
        Optional<Item> right = toItem(rightEntry);
        Optional<Item> front = toItem(frontEntry);

        PotDecorations decorations = new PotDecorations(back, left, right, front);
        ItemStack stack = getParent().getContext().getItemStack();
        if (decorations.equals(PotDecorations.EMPTY)) {
            stack.remove(DataComponents.POT_DECORATIONS);
        } else {
            stack.set(DataComponents.POT_DECORATIONS, decorations);
        }
        cleanComponentTag();
    }

    private Optional<Item> toItem(PotDecorationEntryModel entry) {
        ItemStack stack = entry.getItemStack();
        if (stack.isEmpty()) {
            entry.setValid(true);
            return Optional.empty();
        }
        entry.setValid(true);
        return Optional.of(stack.getItem());
    }

    private void cleanComponentTag() {
        CompoundTag data = getData();
        if (data == null || !data.contains("components")) {
            return;
        }
        CompoundTag components = data.getCompound("components");
        components.remove("minecraft:pot_decorations");
        if (components.isEmpty()) {
            data.remove("components");
        }
    }
}
