package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ItemSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.RaritySelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomModelData;

public class ItemGeneralCategoryModel extends ItemEditorCategoryModel {
    private BooleanEntryModel foodToggleEntry;
    public ItemGeneralCategoryModel(ItemEditorModel editor) {
        super(ModTexts.GENERAL, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        String currentId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int currentCount = stack.getCount();
        int currentDamage = stack.isDamageableItem() ? stack.getDamageValue() : 0;
        boolean isUnbreakable = stack.has(DataComponents.UNBREAKABLE);

        getEntries().add(new ItemSelectionEntryModel(this, ModTexts.ITEM_ID, currentId, this::setItemId));
        getEntries().add(new IntegerEntryModel(this, ModTexts.COUNT, currentCount, this::setCount));
        getEntries().add(new IntegerEntryModel(this, ModTexts.DAMAGE, currentDamage, this::setDamage));
        getEntries().add(new BooleanEntryModel(this, ModTexts.UNBREAKABLE, isUnbreakable, this::setUnbreakable));
        getEntries().add(new RaritySelectionEntryModel(this, ModTexts.gui("rarity"), getRarityString(stack), this::setRarity));
        getEntries().add(new IntegerEntryModel(this, ModTexts.gui("custom_model_data"), getCustomModelData(stack), this::setCustomModelData));
        getEntries().add(new IntegerEntryModel(this, ModTexts.gui("repair_cost"), getRepairCost(stack), this::setRepairCost));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("glint_override"), getGlintOverride(stack), this::setGlintOverride));
        foodToggleEntry = new BooleanEntryModel(this, ModTexts.gui("food_enabled"), getParent().getFoodState().isEnabled(), this::setFoodEnabled);
        getEntries().add(foodToggleEntry);
        // Creative slot lock (1.21 data component has no effect, disabled for now)
        //TODO 要把耐火、虚实弹矢、堆叠上限这些通用开关都排进来
    }

    private void setDamage(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!stack.isDamageableItem()) return;
        int max = stack.getMaxDamage();
        int clamped = Math.max(0, Math.min(value, Math.max(0, max - 1)));
        stack.setDamageValue(clamped);
    }

    private void setUnbreakable(boolean value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value) {
            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        } else {
            stack.remove(DataComponents.UNBREAKABLE);
        }
    }

    private void setCount(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        int clamped = Math.max(1, Math.min(999, value));
        stack.setCount(clamped);
    }

    private void setItemId(String id) {
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item != null) {
                ItemStack old = getParent().getContext().getItemStack();
                int count = old.getCount();
                // Create a fresh stack for the new item id. Components/lore/name are item-specific; keep none by default.
                ItemStack repl = new ItemStack(item, count);
                getParent().handleStackReplaced(repl);
            }
        } catch (Exception ignored) {
        }
    }

    private String getRarityString(ItemStack stack) {
        Rarity r = stack.get(DataComponents.RARITY);
        return r != null ? r.getSerializedName() : "common";
    }

    private void setRarity(String name) {
        ItemStack stack = getParent().getContext().getItemStack();
        Rarity r;
        try {
            String n = name == null ? "" : name.toLowerCase();
            int i = n.indexOf(':');
            if (i >= 0) n = n.substring(i + 1);
            r = switch (n) {
                case "uncommon" -> Rarity.UNCOMMON;
                case "rare" -> Rarity.RARE;
                case "epic" -> Rarity.EPIC;
                default -> Rarity.COMMON;
            };
            if (r == Rarity.COMMON) {
                stack.remove(DataComponents.RARITY);
            } else {
                stack.set(DataComponents.RARITY, r);
            }
        } catch (Exception ignored) {}
    }

    private int getCustomModelData(ItemStack stack) {
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return cmd != null ? cmd.value() : 0;
    }

    private void setCustomModelData(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value > 0) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(value));
        } else {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
    }

    private int getRepairCost(ItemStack stack) {
        Integer v = stack.get(DataComponents.REPAIR_COST);
        return v != null ? v : 0;
    }

    private void setRepairCost(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value > 0) stack.set(DataComponents.REPAIR_COST, value); else stack.remove(DataComponents.REPAIR_COST);
    }

    private boolean getGlintOverride(ItemStack stack) {
        Boolean b = stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        return b != null && b;
    }

    private void setGlintOverride(boolean value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value) stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true); else stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private void setFoodEnabled(boolean value) {
        if (value) {
            getParent().enableFoodComponent();
        } else {
            getParent().disableFoodComponent();
        }
    }

    public void syncFoodToggle() {
        if (foodToggleEntry == null) {
            return;
        }
        boolean enabled = getParent().getFoodState().isEnabled();
        foodToggleEntry.setValue(enabled);
        if (foodToggleEntry.valueChangedProperty().getValue()) {
            foodToggleEntry.apply();
        }
    }
}
