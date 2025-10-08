package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemContainerCategoryModel extends ItemEditorCategoryModel {
    private final List<StringEntryModel> slotEntries = new ArrayList<>();

    public ItemContainerCategoryModel(ItemEditorModel editor) {
        super(ModTexts.CONTAINER_CONTENTS, editor);
    }

    @Override
    protected void setupEntries() {
        slotEntries.clear();
        ItemStack stack = getParent().getContext().getItemStack();
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        //TODO UI还得增强，最好是得让玩家一眼就能设定锁匙
        if (contents != null) {
            contents.stream().filter(item -> !item.isEmpty())
                    .forEach(item -> getEntries().add(createSlotEntry(formatSlot(item))));
        }
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createSlotEntry("");
    }

    private EntryModel createSlotEntry(String spec) {
        StringEntryModel entry = new StringEntryModel(this, ModTexts.CONTAINER_SLOT, spec, value -> { });
        slotEntries.add(entry);
        return entry;
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        List<ItemStack> parsed = new ArrayList<>();
        boolean hasInvalid = false;
        for (StringEntryModel entry : slotEntries) {
            String spec = Optional.ofNullable(entry.getValue()).orElse("").trim();
            if (spec.isBlank()) {
                entry.setValid(true);
                continue;
            }
            Optional<ItemStack> parsedStack = parseSlot(spec);
            if (parsedStack.isPresent()) {
                parsed.add(parsedStack.get());
                entry.setValid(true);
            } else {
                entry.setValid(false);
                hasInvalid = true;
            }
        }
        if (hasInvalid) {
            return;
        }
        if (parsed.isEmpty()) {
            stack.remove(DataComponents.CONTAINER);
        } else {
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(parsed));
        }
        CompoundTag data = getData();
        if (data != null && data.contains("components")) {
            CompoundTag components = data.getCompound("components");
            components.remove("minecraft:container");
            if (components.isEmpty()) {
                data.remove("components");
            }
        }
    }

    private Optional<ItemStack> parseSlot(String spec) {
        try {
            CompoundTag tag = TagParser.parseTag(spec);
            ItemStack parsed = ItemStack.parseOptional(ClientUtil.registryAccess(), tag);
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(parsed);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String formatSlot(ItemStack stack) {
        CompoundTag tag = (CompoundTag) stack.save(ClientUtil.registryAccess(), new CompoundTag());
        return tag.toString();
    }
}
