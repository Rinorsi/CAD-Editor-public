package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.TextEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemDisplayCategoryModel extends ItemEditorCategoryModel {
    private List<MutableComponent> newLore;

    public ItemDisplayCategoryModel(ItemEditorModel editor) {
        super(ModTexts.DISPLAY, editor);
    }

    @Override
    protected void setupEntries() {
        getEntries().add(new TextEntryModel(this, ModTexts.CUSTOM_NAME, getItemName(), this::setItemName));
        ItemLore lore = getStack().get(DataComponents.LORE);
        if (lore != null) {
            lore.lines().stream()
                    .map(Component::copy)
                    .map(this::createLoreEntry)
                    .forEach(getEntries()::add);
        }
    }

    @Override
    public int getEntryListStart() {
        return 1;
    }

    @Override
    public MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.LORE_ADD;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createLoreEntry(null);
    }

    private EntryModel createLoreEntry(MutableComponent value) {
        TextEntryModel entry = new TextEntryModel(this, null, value, this::addLore);
        entry.listIndexProperty().addListener(index -> entry.setLabel(ModTexts.lore(index + 1)));
        return entry;
    }

    @Override
    public void apply() {
        newLore = new ArrayList<>();
        super.apply();
        ItemStack stack = getStack();
        if (!newLore.isEmpty()) {
            List<Component> loreLines = newLore.stream()
                    .filter(Objects::nonNull)
                    .map(MutableComponent::copy)
                    .map(Component.class::cast)
                    .toList();
            stack.set(DataComponents.LORE, new ItemLore(loreLines));
        } else {
            stack.remove(DataComponents.LORE);
        }
    }

    private MutableComponent getItemName() {
        Component component = getStack().get(DataComponents.CUSTOM_NAME);
        return component == null ? null : component.copy();
    }

    private void setItemName(MutableComponent value) {
        if (value == null) {
            getStack().remove(DataComponents.CUSTOM_NAME);
            return;
        }
        if (!value.getString().isEmpty()) {
            if (!value.getSiblings().isEmpty() && value.getContents() instanceof PlainTextContents lc && lc.text().isEmpty()) {
                value.withStyle(style -> style.withItalic(false));
            }
            getStack().set(DataComponents.CUSTOM_NAME, value.copy());
        } else {
            getStack().remove(DataComponents.CUSTOM_NAME);
        }
    }

    private void addLore(MutableComponent value) {
        if (value == null) {
            return;
        }
        if (!value.getString().isEmpty() && value.getContents() instanceof PlainTextContents lc && lc.text().isEmpty() && !value.getSiblings().isEmpty()) {
            value.withStyle(style -> style.withItalic(false).withColor(ChatFormatting.WHITE));
        }
        newLore.add(value);
    }

    private ItemStack getStack() {
        return getParent().getContext().getItemStack();
    }
}
