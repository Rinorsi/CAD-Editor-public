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

import java.util.List;

public class ItemDisplayCategoryModel extends ItemEditorCategoryModel {
    private TextEntryModel itemNameEntry;
    private TextEntryModel customNameEntry;

    public ItemDisplayCategoryModel(ItemEditorModel editor) {
        super(ModTexts.DISPLAY, editor);
    }

    @Override
    protected void setupEntries() {
        itemNameEntry = new TextEntryModel(this, ModTexts.ITEM_NAME, getItemNameOverride(), this::setItemNameOverride);
        customNameEntry = new TextEntryModel(this, ModTexts.CUSTOM_NAME, getCustomName(), this::setCustomName);
        getEntries().add(itemNameEntry);
        getEntries().add(customNameEntry);
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
        return 2;
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
        TextEntryModel entry = new TextEntryModel(this, null, value, lore -> {});
        entry.listIndexProperty().addListener(index -> entry.setLabel(ModTexts.lore(index + 1)));
        return entry;
    }

    @Override
    public void apply() {
        super.apply();
        setItemNameOverride(itemNameEntry.getValue());
        setCustomName(customNameEntry.getValue());
        ItemStack stack = getStack();
        List<Component> loreLines = getLoreEntries().stream()
                .map(TextEntryModel::getValue)
                .map(this::normalizeLoreLine)
                .filter(v -> v != null)
                .map(Component.class::cast)
                .toList();
        if (!loreLines.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(loreLines));
        } else {
            stack.remove(DataComponents.LORE);
        }
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null && customName.getString().isEmpty()) {
            stack.remove(DataComponents.CUSTOM_NAME);
        }
    }

    private MutableComponent getItemNameOverride() {
        Component component = getStack().get(DataComponents.ITEM_NAME);
        if (component != null) {
            return component.copy();
        }
        return Component.translatable(getStack().getDescriptionId()).copy();
    }

    private void setItemNameOverride(MutableComponent value) {
        ItemStack stack = getStack();
        if (isBlank(value)) {
            stack.remove(DataComponents.ITEM_NAME);
            return;
        }
        MutableComponent copy = value.copy();
        if (!copy.getSiblings().isEmpty() && copy.getContents() instanceof PlainTextContents lc && lc.text().isEmpty()) {
            copy = copy.withStyle(style -> style.withItalic(false));
        }
        stack.set(DataComponents.ITEM_NAME, copy);
    }

    private MutableComponent getCustomName() {
        Component component = getStack().get(DataComponents.CUSTOM_NAME);
        return component == null ? null : component.copy();
    }

    private void setCustomName(MutableComponent value) {
        if (isBlank(value)) {
            getStack().remove(DataComponents.CUSTOM_NAME);
            return;
        }
        if (!value.getString().isEmpty()) {
            MutableComponent copy = value.copy();
            if (!copy.getSiblings().isEmpty() && copy.getContents() instanceof PlainTextContents lc && lc.text().isEmpty()) {
                copy = copy.withStyle(style -> style.withItalic(false));
            }
            getStack().set(DataComponents.CUSTOM_NAME, copy);
        } else {
            getStack().remove(DataComponents.CUSTOM_NAME);
        }
    }

    private List<TextEntryModel> getLoreEntries() {
        int start = getEntryListStart();
        if (start < 0) {
            return List.of();
        }
        int end = getEntries().size();
        if (canAddEntryInList()) {
            end -= 1;
        }
        if (start >= end) {
            return List.of();
        }
        return getEntries().subList(start, end).stream()
                .filter(TextEntryModel.class::isInstance)
                .map(TextEntryModel.class::cast)
                .toList();
    }

    private MutableComponent normalizeLoreLine(MutableComponent value) {
        if (value == null) {
            return null;
        }
        MutableComponent copy = value.copy();
        if (copy.getString().isEmpty() && copy.getSiblings().isEmpty()) {
            return null;
        }
        if (!copy.getString().isEmpty() && copy.getContents() instanceof PlainTextContents lc && lc.text().isEmpty() && !copy.getSiblings().isEmpty()) {
            copy = copy.withStyle(style -> style.withItalic(false).withColor(ChatFormatting.WHITE));
        }
        return copy;
    }

    private static boolean isBlank(Component c) {
        return c == null || c.getString().isEmpty();
    }

    private ItemStack getStack() {
        return getParent().getContext().getItemStack();
    }
}
