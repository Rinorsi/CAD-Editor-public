package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ItemSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.RaritySelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.Unbreakable;

public class ItemGeneralCategoryModel extends ItemEditorCategoryModel {
    private static final DamageResistant FIRE_DAMAGE_RESISTANCE = new DamageResistant(DamageTypeTags.IS_FIRE);
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
        getEntries().add(new IntegerEntryModel(this, ModTexts.MAX_STACK_SIZE, getMaxStackSizeValue(stack), this::setMaxStackSize, value -> value >= 0 && value <= Item.ABSOLUTE_MAX_STACK_SIZE));
        getEntries().add(new IntegerEntryModel(this, ModTexts.DAMAGE, currentDamage, this::setDamage));
        getEntries().add(new IntegerEntryModel(this, ModTexts.MAX_DAMAGE, getMaxDamageValue(stack), this::setMaxDamage, value -> value >= 0));
        getEntries().add(new BooleanEntryModel(this, ModTexts.UNBREAKABLE, isUnbreakable, this::setUnbreakable));
        getEntries().add(new RaritySelectionEntryModel(this, ModTexts.gui("rarity"), getRarityString(stack), this::setRarity));
        getEntries().add(new IntegerEntryModel(this, ModTexts.gui("custom_model_data"), getCustomModelData(stack), this::setCustomModelData));
        getEntries().add(new IntegerEntryModel(this, ModTexts.gui("repair_cost"), getRepairCost(stack), this::setRepairCost));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("glint_override"), getGlintOverride(stack), this::setGlintOverride));
        getEntries().add(new BooleanEntryModel(this, ModTexts.FIRE_RESISTANT, isFireResistant(stack), this::setFireResistant));
        getEntries().add(new BooleanEntryModel(this, ModTexts.INTANGIBLE_PROJECTILE, stack.has(DataComponents.INTANGIBLE_PROJECTILE), this::setIntangibleProjectile));
        foodToggleEntry = new BooleanEntryModel(this, ModTexts.gui("food_enabled"), getParent().getFoodState().isEnabled(), this::setFoodEnabled);
        getEntries().add(foodToggleEntry);
        // Creative slot lock (1.21 data component has no effect, disabled for now)
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

    private int getMaxDamageValue(ItemStack stack) {
        Integer override = stack.get(DataComponents.MAX_DAMAGE);
        if (override != null) {
            return override;
        }
        Integer base = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
        return base != null ? base : 0;
    }

    private void setMaxDamage(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        int sanitized = Math.max(0, value);
        Integer base = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
        int baseValue = base != null ? base : 0;
        if (sanitized == 0) {
            stack.remove(DataComponents.MAX_DAMAGE);
        } else if (sanitized == baseValue) {
            stack.remove(DataComponents.MAX_DAMAGE);
        } else {
            stack.set(DataComponents.MAX_DAMAGE, sanitized);
        }
        int max = stack.getMaxDamage();
        if (max > 0) {
            int clamped = Math.max(0, Math.min(stack.getDamageValue(), Math.max(0, max - 1)));
            stack.setDamageValue(clamped);
        } else {
            stack.setDamageValue(0);
            stack.remove(DataComponents.DAMAGE);
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
            BuiltInRegistries.ITEM.getOptional(rl).ifPresent(item -> {
                ItemStack old = getParent().getContext().getItemStack();
                int count = old.getCount();
                ItemStack repl = new ItemStack(item, count);
                getParent().handleStackReplaced(repl);
            });
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
        if (cmd == null || cmd.colors().isEmpty()) {
            return 0;
        }
        Integer first = cmd.colors().get(0);
        return first != null ? first : 0;
    }

    private void setCustomModelData(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value > 0) {
            CustomModelData existing = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            List<Float> floats = existing != null ? existing.floats() : List.of();
            List<Boolean> flags = existing != null ? existing.flags() : List.of();
            List<String> strings = existing != null ? existing.strings() : List.of();
            List<Integer> colors = new ArrayList<>(existing != null ? existing.colors() : List.of());
            if (colors.isEmpty()) {
                colors.add(value);
            } else {
                colors.set(0, value);
            }
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                    List.copyOf(floats),
                    List.copyOf(flags),
                    List.copyOf(strings),
                    List.copyOf(colors)
            ));
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

    private boolean isFireResistant(ItemStack stack) {
        DamageResistant resistant = stack.get(DataComponents.DAMAGE_RESISTANT);
        return resistant != null && resistant.types().equals(DamageTypeTags.IS_FIRE);
    }

    private void setFireResistant(boolean value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value) {
            stack.set(DataComponents.DAMAGE_RESISTANT, FIRE_DAMAGE_RESISTANCE);
        } else {
            DamageResistant existing = stack.get(DataComponents.DAMAGE_RESISTANT);
            if (existing != null && existing.types().equals(DamageTypeTags.IS_FIRE)) {
                stack.remove(DataComponents.DAMAGE_RESISTANT);
            }
        }
    }

    private void setIntangibleProjectile(boolean value) {
        ItemEditorModel parent = getParent();
        ItemStack stack = parent.getContext().getItemStack();
        if (value) {
            stack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.INTANGIBLE_PROJECTILE);
        }
        syncIntangibleProjectileComponent(parent, value);
    }

    private void syncIntangibleProjectileComponent(ItemEditorModel parent, boolean enabled) {
        var context = parent.getContext();
        CompoundTag root = context.getTag();
        if (root == null) {
            return;
        }
        CompoundTag components = root.getCompound("components");
        if (enabled) {
            if (!root.contains("components", Tag.TAG_COMPOUND)) {
                components = new CompoundTag();
                root.put("components", components);
            }
            components.put("minecraft:intangible_projectile", new CompoundTag());
            components.remove("!minecraft:intangible_projectile");
        } else if (root.contains("components", Tag.TAG_COMPOUND)) {
            components.remove("minecraft:intangible_projectile");
            components.remove("!minecraft:intangible_projectile");
            if (components.isEmpty()) {
                root.remove("components");
            }
        }
        context.setTag(root);
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
