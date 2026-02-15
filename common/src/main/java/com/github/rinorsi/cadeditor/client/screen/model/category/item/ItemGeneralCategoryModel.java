package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ItemSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.RaritySelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemGeneralCategoryModel extends ItemEditorCategoryModel {
    private static final String COMPONENTS_KEY = "components";
    private static final String ITEM_MODEL_COMPONENT_KEY = "minecraft:item_model";

    public ItemGeneralCategoryModel(ItemEditorModel editor) {
        super(ModTexts.GENERAL, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        String currentId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int currentCount = stack.getCount();

        getEntries().add(new ItemSelectionEntryModel(this, ModTexts.ITEM_ID, currentId, this::setItemId));
        getEntries().add(new IntegerEntryModel(this, ModTexts.COUNT, currentCount, this::setCount));
        getEntries().add(new IntegerEntryModel(this, ModTexts.MAX_STACK_SIZE, getMaxStackSizeValue(stack), this::setMaxStackSize,
                value -> value >= 0 && value <= Item.ABSOLUTE_MAX_STACK_SIZE));
        getEntries().add(new RaritySelectionEntryModel(this, ModTexts.gui("rarity"), getRarityString(stack), this::setRarity));
        ItemSelectionEntryModel itemModelEntry = new ItemSelectionEntryModel(this, ModTexts.gui("item_model"), getItemModelId(stack), this::setItemModelId);
        itemModelEntry.setPlaceholder("minecraft:diamond");
        getEntries().add(itemModelEntry);
    }

    private void setItemId(String id) {
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            BuiltInRegistries.ITEM.getOptional(rl).ifPresent(item -> {
                ItemStack old = getParent().getContext().getItemStack();
                ItemStack repl = new ItemStack(item, old.getCount());
                getParent().handleStackReplaced(repl);
            });
        } catch (Exception ignored) {
        }
    }

    private void setCount(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        int clamped = Math.max(1, Math.min(999, value));
        stack.setCount(clamped);
    }

    private int getMaxStackSizeValue(ItemStack stack) {
        Integer override = stack.get(DataComponents.MAX_STACK_SIZE);
        return override != null ? override : stack.getItem().getDefaultMaxStackSize();
    }

    private void setMaxStackSize(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        int defaultMax = stack.getItem().getDefaultMaxStackSize();
        if (value <= 0) {
            stack.remove(DataComponents.MAX_STACK_SIZE);
        } else {
            int clamped = Math.max(1, Math.min(Item.ABSOLUTE_MAX_STACK_SIZE, value));
            if (clamped == defaultMax) {
                stack.remove(DataComponents.MAX_STACK_SIZE);
            } else {
                stack.set(DataComponents.MAX_STACK_SIZE, clamped);
            }
        }
        int actualMax = stack.getMaxStackSize();
        if (stack.getCount() > actualMax) {
            stack.setCount(actualMax);
        }
    }

    private String getRarityString(ItemStack stack) {
        Rarity r = stack.get(DataComponents.RARITY);
        return r != null ? r.getSerializedName() : "common";
    }

    private void setRarity(String name) {
        ItemStack stack = getParent().getContext().getItemStack();
        try {
            String n = name == null ? "" : name.toLowerCase();
            int i = n.indexOf(':');
            if (i >= 0) n = n.substring(i + 1);
            Rarity rarity = switch (n) {
                case "uncommon" -> Rarity.UNCOMMON;
                case "rare" -> Rarity.RARE;
                case "epic" -> Rarity.EPIC;
                default -> Rarity.COMMON;
            };
            if (rarity == Rarity.COMMON) {
                stack.remove(DataComponents.RARITY);
            } else {
                stack.set(DataComponents.RARITY, rarity);
            }
        } catch (Exception ignored) {
        }
    }

    private String getItemModelId(ItemStack stack) {
        CompoundTag root = ClientUtil.saveItemStack(ClientUtil.registryAccess(), stack);
        return root.getCompound(COMPONENTS_KEY)
                .flatMap(components -> components.getString(ITEM_MODEL_COMPONENT_KEY))
                .orElse("");
    }

    private void setItemModelId(String value) {
        ItemStack current = getParent().getContext().getItemStack();
        CompoundTag root = ClientUtil.saveItemStack(ClientUtil.registryAccess(), current);
        CompoundTag components = root.getCompound(COMPONENTS_KEY).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            root.put(COMPONENTS_KEY, created);
            return created;
        });

        String normalized = normalizeResourceId(value);
        if (normalized.isEmpty()) {
            components.remove(ITEM_MODEL_COMPONENT_KEY);
        } else {
            components.putString(ITEM_MODEL_COMPONENT_KEY, normalized);
        }
        if (components.isEmpty()) {
            root.remove(COMPONENTS_KEY);
        }

        ItemStack parsed = ClientUtil.parseItemStack(ClientUtil.registryAccess(), root);
        if (!parsed.isEmpty()) {
            getParent().getContext().setItemStack(parsed);
            getParent().getContext().setTag(root);
        }
    }

    private String normalizeResourceId(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        ResourceLocation location = ClientUtil.parseResourceLocation(trimmed);
        return location == null ? "" : location.toString();
    }
}
